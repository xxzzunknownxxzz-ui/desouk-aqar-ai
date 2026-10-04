package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان منشورات المجتمع العقاري الذكي في دسوق
 */
@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorName: String,
    val authorRole: String = "مستخدم", // مستخدم، مكتب عقاري، وسيط معتمد، خبير عقاري
    val authorAvatarRes: String? = null,
    val authorPhone: String? = null,
    val postType: String, // 🏠 عقار للبيع، 🔑 عقار للإيجار، 🔎 مطلوب عقار، 🌾 أرض، 🏢 محل أو وحدة تجارية، 🏬 مكتب أو وحدة إدارية، 💬 سؤال عقاري، 📢 إعلان عقاري، 📰 معلومة أو نصيحة عقارية
    val content: String,
    val imagesJson: String = "", // Comma-separated or JSON list of image resource names
    val hasVideo: Boolean = false,
    val videoLabel: String? = null,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val commentsCount: Int = 0,
    val isSavedByMe: Boolean = false,
    val isFollowed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val district: String? = null,
    // بيانات الاستخراج الذكي AI
    val extractedType: String? = null, // شقة، محل، مكتب، أرض، فيلا، منزل
    val extractedDeal: String? = null, // للبيع، للإيجار، مطلوب
    val extractedLocation: String? = null, // شارع الجيش، دحروج، الصفا...
    val extractedRooms: String? = null,
    val extractedFloor: String? = null,
    val extractedPrice: String? = null,
    val extractedArea: String? = null
)
