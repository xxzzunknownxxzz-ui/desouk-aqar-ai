package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {

    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT * FROM community_posts WHERE id = :id")
    suspend fun getPostById(id: Long): CommunityPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CommunityPostEntity>)

    @Update
    suspend fun updatePost(post: CommunityPostEntity)

    @Query("DELETE FROM community_posts WHERE id = :id")
    suspend fun deletePost(id: Long)

    @Query("SELECT COUNT(*) FROM community_posts")
    suspend fun getPostsCount(): Int

    // التعليقات
    @Query("SELECT * FROM community_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommunityCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommunityCommentEntity): Long

    @Query("UPDATE community_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentsCount(postId: Long)

    @Query("UPDATE community_posts SET likesCount = :newCount, isLikedByMe = :isLiked WHERE id = :postId")
    suspend fun updateLikeStatus(postId: Long, newCount: Int, isLiked: Boolean)

    @Query("UPDATE community_posts SET isSavedByMe = :isSaved WHERE id = :postId")
    suspend fun updateSaveStatus(postId: Long, isSaved: Boolean)

    @Query("UPDATE community_posts SET isFollowed = :isFollowed WHERE id = :postId")
    suspend fun updateFollowStatus(postId: Long, isFollowed: Boolean)
}
