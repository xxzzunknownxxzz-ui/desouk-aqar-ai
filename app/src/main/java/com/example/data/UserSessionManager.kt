package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UserSession(
    val isLoggedIn: Boolean = false,
    val uid: String = "",
    val name: String = "مستخدم عقارات دسوق",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val role: String = UserRole.USER,
    val referralCode: String = "",
    val referredBy: String? = null,
    val hasCompletedOnboarding: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)

class UserSessionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("desouk_user_session_prefs", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<UserSession> = _session.asStateFlow()

    private fun loadSession(): UserSession {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val uid = prefs.getString("uid", "") ?: ""
        val name = prefs.getString("name", "مستخدم عقارات دسوق") ?: "مستخدم عقارات دسوق"
        val email = prefs.getString("email", "") ?: ""
        val phone = prefs.getString("phone", "") ?: ""
        val photoUrl = prefs.getString("photo_url", "") ?: ""
        val role = prefs.getString("role", UserRole.USER) ?: UserRole.USER
        var referralCode = prefs.getString("referral_code", "") ?: ""
        if (referralCode.isBlank() && uid.isNotBlank()) {
            referralCode = generateReferralCode(uid)
            prefs.edit().putString("referral_code", referralCode).apply()
        }
        val referredBy = prefs.getString("referred_by", null)
        val hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
        val createdAt = prefs.getLong("created_at", System.currentTimeMillis())
        val lastLogin = prefs.getLong("last_login", System.currentTimeMillis())

        return UserSession(
            isLoggedIn = isLoggedIn,
            uid = uid,
            name = name,
            email = email,
            phone = phone,
            photoUrl = photoUrl,
            role = role,
            referralCode = referralCode,
            referredBy = referredBy,
            hasCompletedOnboarding = hasCompletedOnboarding,
            createdAt = createdAt,
            lastLogin = lastLogin
        )
    }

    fun saveSession(
        uid: String,
        name: String,
        email: String,
        phone: String = "",
        photoUrl: String = "",
        role: String = UserRole.USER,
        referredBy: String? = null
    ) {
        val cleanRef = if (prefs.getString("referral_code", "").isNullOrBlank()) {
            generateReferralCode(uid)
        } else {
            prefs.getString("referral_code", "")!!
        }

        val now = System.currentTimeMillis()
        val created = if (prefs.getLong("created_at", 0L) > 0L) prefs.getLong("created_at", now) else now

        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("uid", uid)
            .putString("name", name)
            .putString("email", email)
            .putString("phone", phone)
            .putString("photo_url", photoUrl)
            .putString("role", role)
            .putString("referral_code", cleanRef)
            .putString("referred_by", referredBy)
            .putLong("created_at", created)
            .putLong("last_login", now)
            .apply()

        _session.value = UserSession(
            isLoggedIn = true,
            uid = uid,
            name = name,
            email = email,
            phone = phone,
            photoUrl = photoUrl,
            role = role,
            referralCode = cleanRef,
            referredBy = referredBy,
            hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false),
            createdAt = created,
            lastLogin = now
        )
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
        _session.value = _session.value.copy(hasCompletedOnboarding = true)
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putString("uid", "")
            .putString("name", "مستخدم عقارات دسوق")
            .putString("email", "")
            .putString("phone", "")
            .putString("photo_url", "")
            .putString("role", UserRole.USER)
            .apply()

        _session.value = _session.value.copy(
            isLoggedIn = false,
            uid = "",
            name = "مستخدم عقارات دسوق",
            email = "",
            phone = "",
            photoUrl = "",
            role = UserRole.USER
        )
    }

    fun deleteAccount() {
        prefs.edit().clear().apply()
        _session.value = UserSession()
    }

    fun updateProfile(name: String, phone: String) {
        prefs.edit()
            .putString("name", name)
            .putString("phone", phone)
            .apply()
        _session.value = _session.value.copy(name = name, phone = phone)
    }

    private fun generateReferralCode(uid: String): String {
        val short = if (uid.length >= 4) uid.takeLast(4).uppercase() else UUID.randomUUID().toString().take(4).uppercase()
        return "DESOUK-$short"
    }
}
