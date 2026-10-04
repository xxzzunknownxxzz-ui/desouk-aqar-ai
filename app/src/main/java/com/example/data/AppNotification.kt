package com.example.data

/**
 * أنواع الإشعارات داخل التطبيق
 */
enum class NotificationType {
    MATCHING_PROPERTY, // عقار جديد مطابق لاحتياجات المستخدم
    MESSAGE_REPLY,     // تم الرد على رسالة المستخدم
    SYSTEM             // تنبيهات النظام والتحديثات
}

/**
 * كائن الإشعار التفاعلي
 */
data class AppNotification(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val targetPropertyId: Long? = null,
    val targetChatId: Long? = null,
    val categoryBadge: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
