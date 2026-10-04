package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertySourceDao {
    @Query("SELECT * FROM property_sources ORDER BY id DESC")
    fun getAllSources(): Flow<List<PropertySourceEntity>>

    @Query("SELECT * FROM property_sources WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveSources(): Flow<List<PropertySourceEntity>>

    @Query("SELECT * FROM property_sources WHERE id = :id")
    suspend fun getSourceById(id: Long): PropertySourceEntity?

    @Query("SELECT COUNT(*) FROM property_sources WHERE isActive = 1")
    fun getActiveSourcesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM property_sources")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: PropertySourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<PropertySourceEntity>)

    @Update
    suspend fun updateSource(source: PropertySourceEntity)

    @Query("UPDATE property_sources SET isActive = :isActive WHERE id = :id")
    suspend fun toggleActive(id: Long, isActive: Boolean)

    @Query("UPDATE property_sources SET lastSyncTime = :timestamp, totalImportedPosts = totalImportedPosts + :importedCount, newAdsCount = newAdsCount + :newCount, needsReviewCount = needsReviewCount + :reviewCount WHERE id = :id")
    suspend fun updateSyncStats(id: Long, timestamp: Long, importedCount: Int, newCount: Int, reviewCount: Int)

    @Query("DELETE FROM property_sources WHERE id = :id")
    suspend fun deleteSource(id: Long)
}
