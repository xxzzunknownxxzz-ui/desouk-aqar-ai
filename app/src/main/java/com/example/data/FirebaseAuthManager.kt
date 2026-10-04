package com.example.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthManager(
    private val context: Context,
    private val sessionManager: UserSessionManager,
    private val firestoreService: FirestorePropertyService
) {

    private val auth: FirebaseAuth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            FirebaseAuth.getInstance()
        }
    }

    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    /**
     * تسجيل الدخول الرسمي عبر Google / Credential Manager مع الربط بـ Firebase Auth
     * وإنشاء مستخدم جديد في Firestore بعد نجاح الدخول الأول
     */
    suspend fun signInWithGoogle(customContext: Context? = null, webClientId: String? = null): Result<UserSession> = withContext(Dispatchers.IO) {
        val targetContext = customContext ?: context
        try {
            val serverClientId = webClientId?.ifBlank { null }
                ?: "716147702078-app.apps.googleusercontent.com"

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(targetContext, request)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                val authResult = auth.signInWithCredential(authCredential).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val name = firebaseUser.displayName?.ifBlank { null }
                        ?: googleIdTokenCredential.displayName?.ifBlank { null }
                        ?: "مستخدم جوجل"
                    val email = firebaseUser.email ?: googleIdTokenCredential.id
                    val photoUrl = firebaseUser.photoUrl?.toString()
                        ?: googleIdTokenCredential.profilePictureUri?.toString()
                        ?: ""
                    val phone = firebaseUser.phoneNumber ?: ""

                    // 1. إنشاء مستخدم جديد في Firestore لأول مرة أو تحديث آخر دخول وحفظ الدور
                    val syncResult = try {
                        firestoreService.syncOrCreateUserInFirestore(
                            uid = uid,
                            name = name,
                            email = email,
                            phone = phone,
                            photoUrl = photoUrl,
                            provider = "google.com",
                            defaultRole = UserRole.USER
                        ).getOrNull()
                    } catch (_: Exception) {
                        null
                    }

                    val finalRole = syncResult?.first?.role ?: UserRole.USER
                    val finalName = syncResult?.first?.name?.ifBlank { name } ?: name

                    // 2. حفظ بيانات الحساب محلياً في SharedPreferences والجلسة الحالية
                    sessionManager.saveSession(
                        uid = uid,
                        name = finalName,
                        email = email,
                        phone = phone,
                        photoUrl = photoUrl,
                        role = finalRole
                    )

                    return@withContext Result.success(sessionManager.session.value)
                }
            }
            Result.failure(Exception("لم يتم استلام بيانات الحساب من Google"))
        } catch (e: GetCredentialException) {
            // في حالة الإلغاء أو عدم توفر خدمات Google Play على بعض المحاكيات
            val detail = e.message ?: ""
            if (detail.contains("cancelled", ignoreCase = true) || detail.contains("canceled", ignoreCase = true)) {
                Result.failure(Exception("تم إلغاء عملية تسجيل الدخول بواسطة المستخدم."))
            } else {
                Result.failure(Exception("خدمات Google Sign-In غير متوفرة حالياً: ${e.message}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    /**
     * تسجيل الدخول بالبريد الإلكتروني وكلمة المرور
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = authResult.user
            if (user != null) {
                val uid = user.uid
                val name = user.displayName?.ifBlank { null } ?: email.substringBefore("@")
                val role = firestoreService.fetchUserRoleFromFirestore(uid) ?: UserRole.USER

                sessionManager.saveSession(
                    uid = uid,
                    name = name,
                    email = user.email ?: email,
                    role = role
                )
                Result.success(sessionManager.session.value)
            } else {
                Result.failure(Exception("تعذر تسجيل الدخول، يرجى المحاولة لاحقاً."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    /**
     * إنشاء حساب جديد بالبريد الإلكتروني
     */
    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        phone: String
    ): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = authResult.user
            if (user != null) {
                val uid = user.uid
                sessionManager.saveSession(
                    uid = uid,
                    name = name.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    role = UserRole.USER
                )

                try {
                    firestoreService.syncOrCreateUserInFirestore(
                        uid = uid,
                        name = name.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        provider = "password",
                        defaultRole = UserRole.USER
                    )
                } catch (_: Exception) {}

                Result.success(sessionManager.session.value)
            } else {
                Result.failure(Exception("تعذر إنشاء الحساب، يرجى المحاولة لاحقاً."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    /**
     * تسجيل دخول سريع بحساب تجريبي/زائر
     */
    fun signInQuick(name: String, email: String, role: String = UserRole.USER): UserSession {
        val uid = "local_" + email.replace("@", "_").replace(".", "_")
        sessionManager.saveSession(
            uid = uid,
            name = name,
            email = email,
            role = role
        )
        return sessionManager.session.value
    }

    /**
     * تسجيل الخروج
     */
    fun signOut() {
        try {
            auth.signOut()
        } catch (_: Exception) {}
        sessionManager.logout()
    }

    /**
     * حذف الحساب نهائياً
     */
    suspend fun deleteAccount(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            auth.currentUser?.delete()?.await()
            sessionManager.deleteAccount()
            Result.success(true)
        } catch (e: Exception) {
            sessionManager.deleteAccount()
            Result.success(true)
        }
    }

    private fun translateAuthError(e: Exception): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            msg.contains("user-not-found") || msg.contains("no user") -> "البريد الإلكتروني غير مسجل بالتطبيق."
            msg.contains("wrong-password") || msg.contains("invalid credential") -> "كلمة المرور غير صحيحة، يرجى التأكد وإعادة المحاولة."
            msg.contains("email-already-in-use") -> "هذا البريد الإلكتروني مسجل بالفعل بحساب آخر."
            msg.contains("network") || msg.contains("timeout") -> "تعذر الاتصال بالخادم، يرجى التحقق من اتصال الإنترنت."
            msg.contains("weak-password") -> "كلمة المرور ضعيفة، يرجى اختيار كلمة مرور أطول."
            else -> "حدثت مشكلة مؤقتة في تسجيل الدخول. حاول مرة أخرى."
        }
    }
}
