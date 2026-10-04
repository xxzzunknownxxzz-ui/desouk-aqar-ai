package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object UserRole {
    const val SUPER_ADMIN = "SUPER_ADMIN"
    const val ADMIN = "ADMIN"
    const val USER = "USER"
}

@Entity(tableName = "app_users")
data class AppUserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val role: String = UserRole.USER,
    val isBlocked: Boolean = false,
    val propertiesCount: Int = 0,
    val requestsCount: Int = 0,
    val inquiriesCount: Int = 0,
    val city: String = "دسوق",
    val district: String = "وسط البلد",
    val createdAt: Long = System.currentTimeMillis()
)
