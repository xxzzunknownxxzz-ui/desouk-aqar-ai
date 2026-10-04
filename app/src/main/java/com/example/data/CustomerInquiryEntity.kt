package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object InquiryStatus {
    const val NEW = "جديد"
    const val REPLIED = "تم الرد"
    const val IN_PROGRESS = "قيد المتابعة"
    const val CLOSED = "مغلق"
}

@Entity(tableName = "customer_inquiries")
data class CustomerInquiryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userName: String,
    val userPhone: String,
    val propertyId: Long? = null,
    val propertyTitle: String = "استفسار عام عن عقارات دسوق",
    val inquiryText: String,
    val status: String = InquiryStatus.NEW,
    val replyNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
