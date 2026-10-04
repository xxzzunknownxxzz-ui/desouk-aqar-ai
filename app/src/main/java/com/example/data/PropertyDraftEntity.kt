package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان Room لحفظ مسودة العقار الجاري إدخاله محلياً تلقائياً (Auto-Save Draft)
 * لمنع فقدان البيانات عند الخروج من الشاشة أو التنقل بين التبويبات.
 */
@Entity(tableName = "property_drafts")
data class PropertyDraftEntity(
    @PrimaryKey
    val id: Long = 1L,
    val title: String = "",
    val category: String = "شقق",
    val type: String = "للبيع",
    val district: String = "شارع الجيش",
    val addressDetail: String = "",
    val priceText: String = "",
    val areaText: String = "",
    val roomsText: String = "3",
    val bathroomsText: String = "2",
    val floorText: String = "3",
    val phone: String = "01001234567",
    val whatsapp: String = "201001234567",
    val description: String = "",
    val selectedImage: String = "img_property_apartment",
    val updatedAt: Long = System.currentTimeMillis()
)
