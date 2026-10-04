package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * نموذج بيانات اهتمامات المستخدم المفضلة لتلقي إشعارات دفع Firebase Cloud Messaging
 */
data class UserInterests(
    val preferredDistrict: String = "شارع الجيش",
    val maxPrice: Double = 2500000.0,
    val preferredCategory: String = "شقق",
    val preferredDealType: String = "للبيع",
    val isPushNotificationsEnabled: Boolean = true,
    val fcmToken: String = ""
)

/**
 * مدير اهتمامات المستخدم وتكامل اشتراكات موضوعات Firebase Cloud Messaging (FCM Topics)
 */
class UserInterestsPreferenceManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("desouk_user_interests_prefs", Context.MODE_PRIVATE)
    private val tag = "UserInterestsFCM"

    private val _interests = MutableStateFlow(loadInterests())
    val interests: StateFlow<UserInterests> = _interests.asStateFlow()

    init {
        // جلب رمز FCM الفعلي تلقائياً
        fetchAndSyncFcmToken()
        // مزامنة مواضيع FCM بناء على الاهتمامات الحالية
        syncFcmTopics(_interests.value)
    }

    private fun loadInterests(): UserInterests {
        return UserInterests(
            preferredDistrict = prefs.getString("preferred_district", "شارع الجيش") ?: "شارع الجيش",
            maxPrice = prefs.getFloat("max_price", 2500000f).toDouble(),
            preferredCategory = prefs.getString("preferred_category", "شقق") ?: "شقق",
            preferredDealType = prefs.getString("preferred_deal_type", "للبيع") ?: "للبيع",
            isPushNotificationsEnabled = prefs.getBoolean("push_enabled", true),
            fcmToken = prefs.getString("fcm_token", "") ?: ""
        )
    }

    fun updateInterests(newInterests: UserInterests) {
        prefs.edit()
            .putString("preferred_district", newInterests.preferredDistrict)
            .putFloat("max_price", newInterests.maxPrice.toFloat())
            .putString("preferred_category", newInterests.preferredCategory)
            .putString("preferred_deal_type", newInterests.preferredDealType)
            .putBoolean("push_enabled", newInterests.isPushNotificationsEnabled)
            .apply()

        _interests.value = newInterests
        syncFcmTopics(newInterests)
    }

    private fun fetchAndSyncFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    Log.d(tag, "✅ تم الحصول على رمز FCM: $token")
                    prefs.edit().putString("fcm_token", token).apply()
                    _interests.value = _interests.value.copy(fcmToken = token)
                } else {
                    Log.w(tag, "تعذر استخراج رمز FCM الحالي: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "تنبيه FCM: ${e.message}")
        }
    }

    /**
     * الاشتراك في مواضيع FCM المناسبة لاهتمامات المستخدم
     */
    private fun syncFcmTopics(interests: UserInterests) {
        if (!interests.isPushNotificationsEnabled) {
            Log.d(tag, "🔕 تم إيقاف إشعارات الدفع بطلب من المستخدم.")
            return
        }

        try {
            val fcm = FirebaseMessaging.getInstance()
            // الاشتراك في إعلانات دسوق العامة
            fcm.subscribeToTopic("all_desouk_properties")

            // الاشتراك في موضوع المنطقة
            val cleanDistrictTopic = "district_" + interests.preferredDistrict
                .replace(" ", "_")
                .replace("-", "_")
                .trim()
            fcm.subscribeToTopic(cleanDistrictTopic)

            // الاشتراك في موضوع التصنيف
            val categoryTopic = "cat_" + interests.preferredCategory
                .replace(" ", "_")
                .trim()
            fcm.subscribeToTopic(categoryTopic)

            Log.d(tag, "🔔 تم الاشتراك بنجاح في مواضيع FCM: all_desouk_properties, $cleanDistrictTopic, $categoryTopic")
        } catch (e: Exception) {
            Log.w(tag, "تنبيه الاشتراك في مواضيع FCM: ${e.message}")
        }
    }

    /**
     * فحص ما إذا كان العقار الجديد يطابق اهتمامات المستخدم وإرسال إشعار دفع فوري
     */
    fun checkAndNotifyIfMatches(property: PropertyEntity) {
        val current = _interests.value
        if (!current.isPushNotificationsEnabled) return

        val matchesDistrict = current.preferredDistrict == "كل دسوق" ||
                property.district.contains(current.preferredDistrict, ignoreCase = true) ||
                property.location.contains(current.preferredDistrict, ignoreCase = true) ||
                current.preferredDistrict.contains(property.district, ignoreCase = true)

        val matchesCategory = current.preferredCategory == "الكل" ||
                property.category.contains(current.preferredCategory, ignoreCase = true) ||
                current.preferredCategory.contains(property.category, ignoreCase = true)

        val matchesPrice = current.maxPrice <= 0 || property.price <= current.maxPrice

        if (matchesDistrict && matchesCategory && matchesPrice) {
            Log.d(tag, "🎯 تم العثور على عقار مطابق تماماً لاهتمامات المستخدم: ${property.title}")
            DesoukFirebaseMessagingService.sendPushNotification(
                context = context,
                title = "عقار جديد يطابق اهتماماتك في ${property.district} 🎯",
                messageBody = "وجدنا لك: ${property.title} بسعر ${property.price.toInt()} ${property.priceUnit} في موقعك المفضل!",
                propertyId = property.id
            )
        }
    }

    /**
     * محاكاة اختبار إشعار دفع فوري للاهتمامات المحددة
     */
    fun triggerTestPushNotification() {
        val current = _interests.value
        DesoukFirebaseMessagingService.sendPushNotification(
            context = context,
            title = "🔔 تجربة إشعار دفع Firebase (FCM)",
            messageBody = "تنبيهك الذكي مفعل لمنطقة [${current.preferredDistrict}] بسعر حتى ${current.maxPrice.toInt()} ج.م لتصنيف [${current.preferredCategory}]."
        )
    }
}
