package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان تعليقات المجتمع العقاري في دسوق
 */
@Entity(tableName = "community_comments")
data class CommunityCommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val authorRole: String = "مستخدم",
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
