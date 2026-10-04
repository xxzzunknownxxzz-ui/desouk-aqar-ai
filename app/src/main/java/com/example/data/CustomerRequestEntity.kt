package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * حالات طلب العميل
 */
object RequestStatus {
    const val NEW = "جديد"
    const val VIEWED = "تم الاطلاع"
    const val IN_PROGRESS = "جاري المتابعة"
    const val CONTACTED = "تم التواصل"
    const val APPOINTMENT_SET = "تم تحديد موعد معاينة"
    const val COMPLETED = "مكتمل"
    const val CANCELLED = "ملغي"
}

/**
 * أنواع طلبات العملاء
 */
object RequestType {
    const val PURCHASE = "طلب شراء"
    const val RENT = "طلب إيجار"
    const val INSPECTION = "طلب معاينة"
    const val CALL_REQUEST = "طلب اتصال"
    const val INQUIRY = "استفسار عن عقار"
    const val CUSTOM_SEARCH = "طلب بحث عقاري مخصص"
}

/**
 * جدول طلبات العملاء في قاعدة البيانات
 */
@Entity(tableName = "customer_requests")
data class CustomerRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val requestType: String, // نوع الطلب (شراء، إيجار، معاينة، اتصال، استفسار، بحث)
    val propertyId: Long? = null, // العقار المطلوب إذا كان الطلب لعقار معين
    val propertyTitle: String? = null,
    val targetDistrict: String? = null, // المنطقة المطلوبة
    val budget: String? = null, // الميزانية
    val propertyCategory: String? = null, // نوع العقار (شقة، محل، أرض، إلخ)
    val targetArea: String? = null, // المساحة المطلوبة
    val targetRooms: String? = null, // عدد الغرف
    val details: String = "", // تفاصيل وملاحظات الطلب
    val status: String = RequestStatus.NEW, // حالة الطلب
    val isArchived: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
