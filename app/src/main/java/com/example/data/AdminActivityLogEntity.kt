package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * أنواع العمليات الإدارية في سجل العمليات
 */
object AdminActionType {
    const val ADD_PROPERTY = "إضافة عقار"
    const val UPDATE_PROPERTY = "تعديل عقار"
    const val ARCHIVE_PROPERTY = "أرشفة عقار"
    const val RESTORE_PROPERTY = "استرجاع عقار"
    const val DELETE_PROPERTY = "حذف نهائي لعقار"
    const val CHANGE_PROPERTY_STATUS = "تغيير حالة عقار"
    const val CHANGE_REQUEST_STATUS = "تغيير حالة طلب عميل"
    const val ARCHIVE_REQUEST = "أرشفة طلب عميل"
    const val UPDATE_USER_STATUS = "تعديل حالة مستخدم"
    const val DELETE_USER = "حذف مستخدم"
    const val REPLY_INQUIRY = "الرد على استفسار"
    const val UPDATE_SETTINGS = "تحديث إعدادات التطبيق"
    const val LOGIN_SUCCESS = "تسجيل دخول Super Admin"
    const val UNAUTHORIZED_ATTEMPT = "محاولة وصول غير مصرح بها"
}

/**
 * جدول سجل العمليات الإدارية (Activity Log)
 */
@Entity(tableName = "admin_activity_logs")
data class AdminActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String, // نوع العملية
    val details: String,    // تفاصيل العملية
    val targetId: Long? = null, // معرف العقار أو الطلب المتعلق
    val timestamp: Long = System.currentTimeMillis()
)
