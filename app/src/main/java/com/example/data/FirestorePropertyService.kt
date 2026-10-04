package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * خدمة التفاعل مع قاعدة بيانات Firebase Firestore:
 * جلب واسترجاع العقارات ومزامنتها بأمان مع دعم تقنية الإعداد المسبق (Firestore Preloading)
 * والتخزين السحابي المؤقت عالي السرعة (Persistent Offline Cache).
 */
class FirestorePropertyService(private val context: Context? = null) {

    private val tag = "FirestorePropService"

    // حالات الإعداد المسبق لبيانات الصفحة الرئيسية (Firestore Preloading State)
    private val _preloadedProperties = MutableStateFlow<List<FirestoreProperty>>(emptyList())
    val preloadedProperties: StateFlow<List<FirestoreProperty>> = _preloadedProperties.asStateFlow()

    private val _isPreloading = MutableStateFlow(false)
    val isPreloading: StateFlow<Boolean> = _isPreloading.asStateFlow()

    private val _preloadSource = MutableStateFlow("الكاش المحلي (0ms)")
    val preloadSource: StateFlow<String> = _preloadSource.asStateFlow()

    private val _lastPreloadTimestamp = MutableStateFlow(0L)
    val lastPreloadTimestamp: StateFlow<Long> = _lastPreloadTimestamp.asStateFlow()

    private var livePreloadRegistration: ListenerRegistration? = null

    /**
     * تهيئة إعدادات التخزين المؤقت المحلي غير المتصل (Firestore Offline Persistence)
     * لتسريع زمن القراءة الأولي إلى 0 ميلي ثانية فور فتح التطبيق
     */
    fun configureFirestoreCache() {
        if (!isFirebaseAvailable()) return
        try {
            val firestore = FirebaseFirestore.getInstance()
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder()
                            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build()
                    )
                    .build()
                firestore.firestoreSettings = settings
                Log.d(tag, "✅ تم تفعيل إعدادات الكاش المستمر (Persistent Cache) غير المحدود بنجاح.")
            } catch (e: Exception) {
                // بديل متوافق مع إصدارات إعدادات Firestore السابقة
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                firestore.firestoreSettings = settings
                Log.d(tag, "✅ تم تفعيل التخزين المحلي التقليدي (Persistence Enabled) بنجاح.")
            }
        } catch (e: Exception) {
            Log.w(tag, "تنبيه إعدادات كاش Firestore: ${e.message}")
        }
    }

    /**
     * تنفيذ تقنية الإعداد المسبق لبيانات الصفحة الرئيسية (Firestore Preloading):
     * 1. المرحلة الأولى: استرجاع فوري من الكاش المحلي (Source.CACHE) بزمن 0ms بدون انتظار الشبكة.
     * 2. المرحلة الثانية: استرجاع غير متزامن في الخلفية من السيرفر (Source.SERVER) لتحديث الكاش بأحدث العقارات.
     */
    suspend fun preloadHomeScreenProperties(
        limit: Long = 30,
        onPropertiesPreloaded: ((List<FirestoreProperty>, isFromCache: Boolean) -> Unit)? = null
    ): List<FirestoreProperty> {
        if (!isFirebaseAvailable()) return emptyList()

        _isPreloading.value = true
        var resultList = emptyList<FirestoreProperty>()

        try {
            val firestore = FirebaseFirestore.getInstance()
            val query = firestore.collection("properties")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)

            // المرحلة الأولى: فحص واستخراج البيانات من الكاش المحلي فورياً (Instant Cache-First)
            try {
                val cacheSnapshot = query.get(Source.CACHE).await()
                if (!cacheSnapshot.isEmpty) {
                    val cached = cacheSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestoreProperty::class.java)?.copy(id = doc.id)
                    }
                    if (cached.isNotEmpty()) {
                        resultList = cached
                        _preloadedProperties.value = cached
                        _preloadSource.value = "الكاش المحلي السريع (0ms)"
                        _lastPreloadTimestamp.value = System.currentTimeMillis()
                        onPropertiesPreloaded?.invoke(cached, true)
                        Log.d(tag, "⚡ [Preload Stage 1] تم استرجاع ${cached.size} عقار فوراً من الكاش المحلي")
                    }
                }
            } catch (e: Exception) {
                Log.d(tag, "[Preload Stage 1] الكاش فارغ حالياً، سيتم الجلب من السيرفر مباشرة: ${e.message}")
            }

            // المرحلة الثانية: التحديث في الخلفية من سحابة Firestore لضمان الحداثة (Background Server Refresh)
            try {
                val serverSnapshot = query.get(Source.SERVER).await()
                if (!serverSnapshot.isEmpty) {
                    val serverList = serverSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestoreProperty::class.java)?.copy(id = doc.id)
                    }
                    if (serverList.isNotEmpty()) {
                        resultList = serverList
                        _preloadedProperties.value = serverList
                        _preloadSource.value = "السحابة المباشرة (Firestore Live)"
                        _lastPreloadTimestamp.value = System.currentTimeMillis()
                        onPropertiesPreloaded?.invoke(serverList, false)
                        Log.d(tag, "☁️ [Preload Stage 2] تم تحديث ${serverList.size} عقار من سحابة Firestore")
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "[Preload Stage 2] تعذر الاتصال بالسيرفر، الاستمرار بالكاش: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e(tag, "خطأ غير متوقع أثناء Firestore Preloading: ${e.message}")
        } finally {
            _isPreloading.value = false
        }

        return resultList
    }

    /**
     * تشغيل مستمع فوري (Realtime Snapshot Preloader) يبقي بيانات الصفحة الرئيسية محدثة دائماً
     */
    fun startRealtimePreloadListener(
        limit: Long = 30,
        onLiveUpdate: (List<FirestoreProperty>) -> Unit
    ) {
        if (!isFirebaseAvailable()) return
        try {
            livePreloadRegistration?.remove()
            val firestore = FirebaseFirestore.getInstance()
            livePreloadRegistration = firestore.collection("properties")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "خطأ في مستمع Preload اللحظي: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(FirestoreProperty::class.java)?.copy(id = doc.id)
                        }
                        if (list.isNotEmpty()) {
                            _preloadedProperties.value = list
                            _preloadSource.value = if (snapshot.metadata.isFromCache) "الكاش المحلي (0ms)" else "سحابي لحظي"
                            _lastPreloadTimestamp.value = System.currentTimeMillis()
                            onLiveUpdate(list)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "تعذر تشغيل مستمع التحديث اللحظي: ${e.message}")
        }
    }

    /**
     * إيقاف مستمع التحديث اللحظي لمنع تسريب الموارد
     */
    fun stopRealtimePreloadListener() {
        livePreloadRegistration?.remove()
        livePreloadRegistration = null
    }

    /**
     * التحقق من تهيئة Firebase بأمان لتجنب الأخطاء عند عدم توفر google-services.json
     */
    fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getApps(context ?: return FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()).isNotEmpty()
        } catch (e: Exception) {
            try {
                FirebaseFirestore.getInstance()
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    /**
     * استرجاع قائمة العقارات المخزنة في مجموعة "properties" في Firestore
     */
    suspend fun getProperties(): List<FirestoreProperty> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("properties")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreProperty::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.w(tag, "تعذر الجلب المباشر من Firestore: ${e.message}")
            emptyList()
        }
    }

    /**
     * التحقق من دور المستخدم داخل مجموعة المستخدمين (Users collection) في Firestore
     */
    suspend fun verifyUserRoleInFirestore(userId: String): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users").document(userId).get().await()
            val role = userDoc.getString("role")
            role.equals("Admin", ignoreCase = true) || role.equals("SUPER_ADMIN", ignoreCase = true)
        } catch (e: Exception) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val userDoc = firestore.collection("Users").document(userId).get().await()
                val role = userDoc.getString("role")
                role.equals("Admin", ignoreCase = true) || role.equals("SUPER_ADMIN", ignoreCase = true)
            } catch (ex: Exception) {
                Log.w(tag, "تعذر التحقق من دور المستخدم في Firestore: ${ex.message}")
                false
            }
        }
    }

    /**
     * إضافة أو رفع عقار جديد إلى مجموعة Firestore (مقيدة بحساب الأدمن)
     */
    suspend fun addProperty(
        property: FirestoreProperty,
        isAdminAuthenticated: Boolean = true
    ): Result<String> {
        if (!isAdminAuthenticated) {
            val securityError = SecurityException("غير مصرح: إضافة العقارات في Firestore تتطلب امتلاك دور 'Admin' في مجموعة المستخدمين (Users collection).")
            Log.e(tag, "انتهاك أمني: محاولة إضافة عقار بدون دور Admin")
            return Result.failure(securityError)
        }

        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection("properties")
                .add(property.toMap())
                .await()

            logAdminAuditToFirestore(
                action = "إضافة عقار في Firestore",
                details = "تمت إضافة العقار '${property.title}' بواسطة مستخدم بدور Admin",
                targetId = docRef.id
            )
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(tag, "فشل حفظ العقار في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * التحقق من الصلاحيات وتعديل بيانات العقار في Firestore
     * العمليات الحساسة مقيدة حصراً بحساب الأدمن
     */
    suspend fun updateProperty(
        propertyId: String,
        property: FirestoreProperty,
        isAdminAuthenticated: Boolean
    ): Result<Unit> {
        // التحقق الأمني من صلاحيات الأدمن
        if (!isAdminAuthenticated) {
            val securityError = SecurityException("غير مصرح: عملية تعديل العقارات في Firestore مقيدة بحساب الأدمن فقط.")
            Log.e(tag, "انتهاك أمني: محاولة تعديل عقار $propertyId بدون صلاحيات Admin")
            return Result.failure(securityError)
        }

        return try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("properties")
                .document(propertyId)
                .update(property.toMap())
                .await()

            logAdminAuditToFirestore(
                action = "تعديل عقار في Firestore",
                details = "تم تعديل بيانات العقار '$propertyId' بواسطة Super Admin",
                targetId = propertyId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "فشل تعديل العقار في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * التحقق من الصلاحيات وتغيير حالة العقار في Firestore (منشور / مؤرشف / مباع)
     */
    suspend fun updatePropertyStatus(
        propertyId: String,
        newStatus: String,
        isAdminAuthenticated: Boolean
    ): Result<Unit> {
        if (!isAdminAuthenticated) {
            val securityError = SecurityException("غير مصرح: تغيير حالة العقار في Firestore يتطلب صلاحيات الأدمن.")
            return Result.failure(securityError)
        }

        return try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("properties")
                .document(propertyId)
                .update("status", newStatus)
                .await()

            logAdminAuditToFirestore(
                action = "تغيير حالة عقار في Firestore",
                details = "تم تغيير حالة العقار $propertyId إلى: $newStatus",
                targetId = propertyId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "فشل تحديث حالة العقار في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * التحقق من الصلاحيات وحذف العقار نهائياً من Firestore
     * الحذف عملية شديدة الحساسية ومحصورة بالأدمن فقط
     */
    suspend fun deleteProperty(
        propertyId: String,
        propertyTitle: String,
        isAdminAuthenticated: Boolean
    ): Result<Unit> {
        // التحقق الأمني الصارم
        if (!isAdminAuthenticated) {
            val securityError = SecurityException("رفض العملية: حذف العقارات من Firestore محظور تماماً لغير حسابات الأدمن المعتمدة.")
            Log.e(tag, "محاولة حذف غير مصرح بها للعقار $propertyId ($propertyTitle)")
            return Result.failure(securityError)
        }

        return try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("properties")
                .document(propertyId)
                .delete()
                .await()

            logAdminAuditToFirestore(
                action = "حذف عقار من Firestore",
                details = "تم حذف العقار '$propertyTitle' (ID: $propertyId) نهائياً بواسطة Super Admin",
                targetId = propertyId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "فشل حذف العقار من Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * تسجيل العملية الإدارية في سجل التدقيق السحابي في Firestore (admin_activity_logs)
     */
    suspend fun logAdminAuditToFirestore(
        action: String,
        details: String,
        targetId: String? = null
    ): Result<Unit> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val logData = mapOf(
                "action" to action,
                "details" to details,
                "targetId" to (targetId ?: ""),
                "performedBy" to "Super Admin",
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("admin_activity_logs")
                .add(logData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "تعذر تسجيل التدقيق السحابي في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    data class FirestoreUserData(
        val uid: String = "",
        val name: String = "",
        val email: String = "",
        val phone: String = "",
        val photoUrl: String = "",
        val role: String = "User",
        val provider: String = "google.com",
        val createdAt: Long = System.currentTimeMillis(),
        val lastLoginAt: Long = System.currentTimeMillis(),
        val isFirstLogin: Boolean = false,
        val savedPropertiesCount: Int = 0,
        val savedSearchesCount: Int = 0,
        val viewedPropertiesCount: Int = 0,
        val myAdsCount: Int = 0,
        val city: String = "دسوق",
        val status: String = "active"
    )

    /**
     * التحقق من وجود المستخدم في Firestore:
     * - إذا كان المستخدم يسجل لأول مرة: يتم إنشاء مستخدم جديد بكامل بياناته تلقائياً في مجموعة "users".
     * - إذا كان المستخدم مسجلاً مسبقاً: يتم تحديث تاريخ آخر تسجيل دخول lastLoginAt وحفظ دوره الحالي.
     */
    suspend fun syncOrCreateUserInFirestore(
        uid: String,
        name: String,
        email: String,
        phone: String = "",
        photoUrl: String = "",
        provider: String = "google.com",
        defaultRole: String = "User"
    ): Result<Pair<FirestoreUserData, Boolean>> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(uid)
            val snapshot = userDocRef.get().await()

            val now = System.currentTimeMillis()

            if (!snapshot.exists()) {
                // مستخدم جديد يسجل لأول مرة في التطبيق
                val cleanName = name.ifBlank { "مستخدم عقارات دسوق" }
                val newUserData = hashMapOf<String, Any>(
                    "uid" to uid,
                    "name" to cleanName,
                    "email" to email,
                    "phone" to phone,
                    "photoUrl" to photoUrl,
                    "role" to defaultRole,
                    "provider" to provider,
                    "createdAt" to now,
                    "lastLoginAt" to now,
                    "savedPropertiesCount" to 0,
                    "savedSearchesCount" to 0,
                    "viewedPropertiesCount" to 0,
                    "myAdsCount" to 0,
                    "city" to "دسوق",
                    "status" to "active"
                )

                userDocRef.set(newUserData).await()

                logAdminAuditToFirestore(
                    action = "إنشاء مستخدم جديد في Firestore",
                    details = "تم إنشاء مستخدم جديد لأول مرة عبر Google Sign-In ($email) بالمعرف $uid",
                    targetId = uid
                )

                val createdUser = FirestoreUserData(
                    uid = uid,
                    name = cleanName,
                    email = email,
                    phone = phone,
                    photoUrl = photoUrl,
                    role = defaultRole,
                    provider = provider,
                    createdAt = now,
                    lastLoginAt = now,
                    isFirstLogin = true
                )
                Result.success(Pair(createdUser, true))
            } else {
                // مستخدم موجود بالفعل: تحديث آخر دخول والحفاظ على الدور الموجود (Admin / User)
                val existingRole = snapshot.getString("role") ?: defaultRole
                val existingCreated = snapshot.getLong("createdAt") ?: now
                val updates = mutableMapOf<String, Any>(
                    "lastLoginAt" to now
                )
                if (photoUrl.isNotBlank() && snapshot.getString("photoUrl").isNullOrBlank()) {
                    updates["photoUrl"] = photoUrl
                }
                if (name.isNotBlank() && (snapshot.getString("name").isNullOrBlank() || snapshot.getString("name") == "مستخدم عقارات دسوق")) {
                    updates["name"] = name
                }
                userDocRef.set(updates, com.google.firebase.firestore.SetOptions.merge()).await()

                val existingUser = FirestoreUserData(
                    uid = uid,
                    name = snapshot.getString("name")?.ifBlank { name } ?: name,
                    email = snapshot.getString("email") ?: email,
                    phone = snapshot.getString("phone") ?: phone,
                    photoUrl = snapshot.getString("photoUrl") ?: photoUrl,
                    role = existingRole,
                    provider = snapshot.getString("provider") ?: provider,
                    createdAt = existingCreated,
                    lastLoginAt = now,
                    isFirstLogin = false,
                    savedPropertiesCount = snapshot.getLong("savedPropertiesCount")?.toInt() ?: 0,
                    savedSearchesCount = snapshot.getLong("savedSearchesCount")?.toInt() ?: 0,
                    viewedPropertiesCount = snapshot.getLong("viewedPropertiesCount")?.toInt() ?: 0,
                    myAdsCount = snapshot.getLong("myAdsCount")?.toInt() ?: 0
                )
                Result.success(Pair(existingUser, false))
            }
        } catch (e: Exception) {
            Log.e(tag, "فشل إنشاء أو مزامنة المستخدم في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * حفظ وتخزين دور المستخدم (Admin vs User) في مجموعة المستخدمين (Users collection) في Firestore
     */
    suspend fun saveUserRoleToFirestore(
        userId: String,
        name: String,
        email: String,
        phone: String,
        role: String // "Admin" أو "User"
    ): Result<Unit> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val userData = mapOf(
                "uid" to userId,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "role" to role,
                "updatedAt" to System.currentTimeMillis()
            )
            // حفظ في مجموعة users
            firestore.collection("users")
                .document(userId)
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .await()

            logAdminAuditToFirestore(
                action = "تحديث دور مستخدم في Firestore",
                details = "تم تسجيل وتخزين دور '$role' للمستخدم '$name' ($email) في Users collection",
                targetId = userId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "فشل حفظ دور المستخدم في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * التحقق من دور المستخدم المسجل في Firestore واسترجاعه
     */
    suspend fun fetchUserRoleFromFirestore(userId: String): String? {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                userDoc.getString("role")
            } else {
                val altDoc = firestore.collection("Users").document(userId).get().await()
                if (altDoc.exists()) altDoc.getString("role") else null
            }
        } catch (e: Exception) {
            Log.w(tag, "تعذر استرجاع دور المستخدم من Firestore: ${e.message}")
            null
        }
    }

    // ==============================================================
    // إدارة مصادر العقارات والمنشورات الخام في Firestore (Admin Protected)
    // ==============================================================

    /**
     * استرجاع قائمة المصادر المعتمدة من مجموعة sources في Firestore
     */
    suspend fun getSourcesFromFirestore(): List<FirestoreSource> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("sources")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreSource::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            Log.w(tag, "تعذر جلب المصادر من Firestore: ${e.message}")
            emptyList()
        }
    }

    /**
     * إضافة أو تحديث مصدر عقاري في مجموعة sources بـ Firestore
     */
    suspend fun saveSourceToFirestore(
        source: FirestoreSource,
        isAdminAuthenticated: Boolean = true
    ): Result<String> {
        if (!isAdminAuthenticated) {
            return Result.failure(SecurityException("غير مصرح: إدارة المصادر في Firestore مقيدة بحساب الأدمن."))
        }
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = if (source.id.isNotBlank()) {
                firestore.collection("sources").document(source.id).also { it.set(source.toMap()).await() }
            } else {
                firestore.collection("sources").add(source.toMap()).await()
            }

            logAdminAuditToFirestore(
                action = "تحديث مصدر في Firestore",
                details = "تم حفظ المصدر '${source.name}' بنجاح",
                targetId = docRef.id
            )
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(tag, "فشل حفظ المصدر في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * تفعيل أو تعطيل مصدر في Firestore
     */
    suspend fun toggleSourceActiveInFirestore(
        sourceId: String,
        isActive: Boolean,
        isAdminAuthenticated: Boolean
    ): Result<Unit> {
        if (!isAdminAuthenticated) {
            return Result.failure(SecurityException("غير مصرح: تعديل حالة المصادر مقيد بالأدمن."))
        }
        return try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("sources").document(sourceId)
                .update("isActive", isActive)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "فشل تعديل حالة المصدر: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * حفظ المنشور الخام في مجموعة raw_posts دون أي تعديل على النص الأصلي
     */
    suspend fun saveRawPostToFirestore(rawPost: FirestoreRawPost): Result<String> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection("raw_posts")
                .add(rawPost.toMap())
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.w(tag, "تعذر حفظ المنشور الخام في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * استرجاع الإعلانات المستوردة وقائمة المراجعة من مجموعة imported_properties في Firestore
     */
    suspend fun getImportedPropertiesFromFirestore(): List<FirestoreImportedProperty> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("imported_properties")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreImportedProperty::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            Log.w(tag, "تعذر جلب الإعلانات المستوردة من Firestore: ${e.message}")
            emptyList()
        }
    }

    /**
     * حفظ أو تحديث إعلان مستورد في مجموعة imported_properties بـ Firestore
     */
    suspend fun saveImportedPropertyToFirestore(
        imported: FirestoreImportedProperty
    ): Result<String> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = if (imported.id.isNotBlank()) {
                firestore.collection("imported_properties").document(imported.id).also { it.set(imported.toMap()).await() }
            } else {
                firestore.collection("imported_properties").add(imported.toMap()).await()
            }
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(tag, "فشل حفظ الإعلان المستورد في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * تحديث حالة إعلان مستورد في Firestore (اعتماد، رفض، أرشفة)
     */
    suspend fun updateImportedStatusInFirestore(
        docId: String,
        newStatus: String,
        notes: String,
        isAdminAuthenticated: Boolean = true
    ): Result<Unit> {
        if (!isAdminAuthenticated) {
            return Result.failure(SecurityException("غير مصرح بالأدمن."))
        }
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val updates = mapOf(
                "status" to newStatus,
                "reviewNotes" to notes,
                "reviewedAt" to System.currentTimeMillis()
            )
            firestore.collection("imported_properties").document(docId)
                .update(updates)
                .await()

            logAdminAuditToFirestore(
                action = "مراجعة إعلان مستورد",
                details = "تم تغيير حالة الإعلان المستورد $docId إلى: $newStatus",
                targetId = docId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "فشل تحديث حالة الإعلان في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * حساب إحصائيات لوحة مصادر العقارات من مجموعات Firestore السحابية
     */
    suspend fun fetchSourcesDashboardStatsFromFirestore(): SourcesDashboardStats? {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            val sourcesSnap = firestore.collection("sources").get().await()
            val activeSources = sourcesSnap.documents.count { it.getBoolean("isActive") == true }

            val rawPostsSnap = firestore.collection("raw_posts").get().await()
            val totalRaw = rawPostsSnap.size()

            val importedSnap = firestore.collection("imported_properties").get().await()
            val inReview = importedSnap.documents.count { it.getString("status") == "قيد المراجعة" }
            val published = importedSnap.documents.count { it.getString("status") == "نشط" }
            val duplicates = importedSnap.documents.count { it.getBoolean("isDuplicate") == true || it.getString("status") == "مكرر" }
            val rejected = importedSnap.documents.count { it.getString("status") == "مرفوض" }
            val archived = importedSnap.documents.count { it.getString("status") == "مؤرشف" }
            val newAds = importedSnap.documents.count { it.getString("status") == "قيد المراجعة" || it.getString("status") == "جديد" }

            SourcesDashboardStats(
                activeSourcesCount = activeSources,
                totalImportedPostsCount = totalRaw,
                newAdsCount = newAds,
                inReviewCount = inReview,
                publishedCount = published,
                duplicateCount = duplicates,
                rejectedCount = rejected,
                archivedCount = archived
            )
        } catch (e: Exception) {
            Log.w(tag, "تعذر حساب إحصائيات Firestore: ${e.message}")
            null
        }
    }

    /**
     * حفظ ونشر تقييم ومراجعة العقار في سحابة Firestore
     */
    suspend fun addReviewToFirestore(
        propertyId: Long,
        userName: String,
        userRole: String,
        rating: Int,
        comment: String
    ): Result<FirestoreReview> {
        if (!isFirebaseAvailable()) {
            return Result.failure(IllegalStateException("خدمات Firebase غير متصلة حالياً."))
        }
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection("property_reviews").document()
            val reviewData = hashMapOf(
                "id" to docRef.id,
                "propertyId" to propertyId,
                "userName" to userName.ifBlank { "مستخدم في دسوق" },
                "userRole" to userRole.ifBlank { "زائر معاينة" },
                "rating" to rating.coerceIn(1, 5),
                "comment" to comment.trim(),
                "timestamp" to System.currentTimeMillis()
            )
            docRef.set(reviewData).await()
            Log.d(tag, "⭐ تم حفظ مراجعة العقار $propertyId بنجاح في Firestore (Doc: ${docRef.id})")

            val review = FirestoreReview(
                id = docRef.id,
                propertyId = propertyId,
                userName = userName,
                userRole = userRole,
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
            Result.success(review)
        } catch (e: Exception) {
            Log.e(tag, "فشل حفظ التقييم في Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * استرجاع التقييمات والمراجعات المخزنة في Firestore لعقار محدد
     */
    suspend fun fetchReviewsForPropertyFromFirestore(propertyId: Long): List<PropertyReviewEntity> {
        if (!isFirebaseAvailable()) return emptyList()
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("property_reviews")
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val pId = doc.getLong("propertyId") ?: propertyId
                val name = doc.getString("userName") ?: "مستخدم في دسوق"
                val role = doc.getString("userRole") ?: "زائر معاينة"
                val rating = doc.getLong("rating")?.toInt() ?: 5
                val comment = doc.getString("comment") ?: ""
                val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()

                PropertyReviewEntity(
                    id = doc.id.hashCode().toLong(),
                    propertyId = pId,
                    userName = name,
                    userRole = role,
                    rating = rating,
                    comment = comment,
                    timestamp = ts
                )
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.w(tag, "تعذر جلب التقييمات من Firestore: ${e.message}")
            emptyList()
        }
    }

    /**
     * الاستماع اللحظي للتقييمات الجديدة في Firestore لعقار محدد
     */
    fun listenToPropertyReviews(
        propertyId: Long,
        onReviewsUpdated: (List<PropertyReviewEntity>) -> Unit
    ): ListenerRegistration? {
        if (!isFirebaseAvailable()) return null
        return try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("property_reviews")
                .whereEqualTo("propertyId", propertyId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "خطأ الاستماع لتقييمات Firestore: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val reviews = snapshot.documents.mapNotNull { doc ->
                            val pId = doc.getLong("propertyId") ?: propertyId
                            val name = doc.getString("userName") ?: "مستخدم في دسوق"
                            val role = doc.getString("userRole") ?: "زائر معاينة"
                            val rating = doc.getLong("rating")?.toInt() ?: 5
                            val comment = doc.getString("comment") ?: ""
                            val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            PropertyReviewEntity(
                                id = doc.id.hashCode().toLong(),
                                propertyId = pId,
                                userName = name,
                                userRole = role,
                                rating = rating,
                                comment = comment,
                                timestamp = ts
                            )
                        }.sortedByDescending { it.timestamp }
                        onReviewsUpdated(reviews)
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "خطأ الاستماع لتقييمات Firestore: ${e.message}")
            null
        }
    }
    /**
     * حفظ بلاغ ضد عقار مخالف في Firestore
     */
    suspend fun reportPropertyToFirestore(
        propertyId: Long,
        propertyTitle: String,
        reason: String,
        details: String,
        reporterUid: String,
        reporterEmail: String
    ): Result<Unit> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val reportData = hashMapOf(
                "propertyId" to propertyId,
                "propertyTitle" to propertyTitle,
                "reason" to reason,
                "details" to details,
                "reporterUid" to reporterUid,
                "reporterEmail" to reporterEmail,
                "timestamp" to System.currentTimeMillis(),
                "status" to "قيد المراجعة"
            )
            firestore.collection("property_reports").add(reportData).await()
            logAdminAuditToFirestore(
                action = "إبلاغ عن عقار",
                details = "تم تقديم بلاغ عن العقار #$propertyId ($propertyTitle) بسبب: $reason",
                targetId = propertyId.toString()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * نموذج تقييم ومراجعة العقار في Firestore
 */
data class FirestoreReview(
    val id: String = "",
    val propertyId: Long = 0L,
    val userName: String = "",
    val userRole: String = "مشتري موثق",
    val rating: Int = 5,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
