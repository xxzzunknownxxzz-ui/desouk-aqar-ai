package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "property_reviews")
data class PropertyReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long,
    val userName: String,
    val userRole: String = "مشتري موثق", // مشتري موثق, مستأجر سابق, زائر معاينة
    val rating: Int, // 1 to 5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
