package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * حالات العقار الإدارية
 */
object PropertyStatus {
    const val DRAFT = "مسودة"
    const val PUBLISHED = "منشور"
    const val UNAVAILABLE = "غير متاح"
    const val SOLD = "مباع"
    const val RENTED = "مؤجر"
    const val ARCHIVED = "مؤرشف"
}

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // شقق, منازل, محلات, مكاتب, أراضي
    val type: String, // للبيع, للإيجار
    val location: String, // e.g. دسوق - شارع الشركات
    val district: String = "دسوق", // District name
    val street: String = "", // الشارع
    val price: Double,
    val priceUnit: String = "ج.م", // ج.م, ج.م / شهريا
    val rooms: Int = 0,
    val bathrooms: Int = 0,
    val area: Double = 0.0, // in m²
    val floor: Int = 1,
    val totalFloors: Int = 5, // عدد الأدوار الكلي
    val finishing: String = "سوبر لوكس", // سوبر لوكس, ألترا لوكس, نصف تشطيب, على المحارة
    val hasElevator: Boolean = true, // مصعد
    val hasGarage: Boolean = false, // جراج
    val hasMeters: Boolean = true, // عدادات (كهرباء، مياه، غاز)
    val features: String = "", // المميزات
    val videoUrl: String = "", // فيديو العقار إن وجد
    val imageResName: String = "img_property_apartment", // img_property_apartment, img_property_building, img_property_shop, img_property_land
    val phone: String = "01001234567",
    val whatsapp: String = "201001234567",
    val description: String = "",
    val status: String = PropertyStatus.PUBLISHED, // مسودة، منشور، غير متاح، مباع، مؤجر، مؤرشف
    val isFavorite: Boolean = false,
    val isUserAdded: Boolean = false,
    val latitude: Double = 31.1306,
    val longitude: Double = 30.6482,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String = "",
    val ownerName: String = "",
    val streetsCount: Int = 1,
    val facade: String = "",
    val sourceUrl: String = "",
    val sourcePlatform: String = "Facebook",
    val importedBy: String = "",
    val importedAt: Long = 0L,
    val isDuplicate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val ownerId: String = "",
    val isNegotiable: Boolean = true,
    val imagesJson: String = ""
)
