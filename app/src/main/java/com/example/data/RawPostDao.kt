package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RawPostDao {
    @Query("SELECT * FROM raw_posts ORDER BY postTimestamp DESC")
    fun getAllRawPosts(): Flow<List<RawPostEntity>>

    @Query("SELECT * FROM raw_posts WHERE sourceId = :sourceId ORDER BY postTimestamp DESC")
    fun getRawPostsBySource(sourceId: Long): Flow<List<RawPostEntity>>

    @Query("SELECT * FROM raw_posts WHERE postUrl = :url OR (postExternalId != '' AND postExternalId = :externalId) LIMIT 1")
    suspend fun findExistingPost(url: String, externalId: String): RawPostEntity?

    @Query("SELECT COUNT(*) FROM raw_posts")
    fun getTotalRawPostsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM raw_posts")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawPost(post: RawPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawPosts(posts: List<RawPostEntity>)

    @Query("DELETE FROM raw_posts WHERE id = :id")
    suspend fun deleteRawPost(id: Long)
}
