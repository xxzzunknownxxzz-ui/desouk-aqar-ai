package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedPropertyDao {
    @Query("SELECT * FROM imported_properties ORDER BY createdAt DESC")
    fun getAllImportedProperties(): Flow<List<ImportedPropertyEntity>>

    @Query("SELECT * FROM imported_properties WHERE status = :status ORDER BY createdAt DESC")
    fun getPropertiesByStatus(status: String): Flow<List<ImportedPropertyEntity>>

    @Query("SELECT * FROM imported_properties WHERE status = 'قيد المراجعة' ORDER BY createdAt DESC")
    fun getReviewQueue(): Flow<List<ImportedPropertyEntity>>

    @Query("SELECT * FROM imported_properties WHERE id = :id")
    suspend fun getPropertyById(id: Long): ImportedPropertyEntity?

    @Query("SELECT * FROM imported_properties WHERE (contactPhone != '' AND contactPhone = :phone) OR (postUrl != '' AND postUrl = :url) LIMIT 5")
    suspend fun findDuplicates(phone: String, url: String): List<ImportedPropertyEntity>

    // Counts for Statistics Cards
    @Query("SELECT COUNT(*) FROM imported_properties WHERE status = 'نشط'")
    fun getPublishedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM imported_properties WHERE status = 'قيد المراجعة'")
    fun getReviewQueueCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM imported_properties WHERE isDuplicate = 1 OR status = 'مكرر'")
    fun getDuplicateCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM imported_properties WHERE status = 'مرفوض'")
    fun getRejectedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM imported_properties WHERE status = 'مؤرشف'")
    fun getArchivedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM imported_properties")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(property: ImportedPropertyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(properties: List<ImportedPropertyEntity>)

    @Update
    suspend fun update(property: ImportedPropertyEntity)

    @Query("UPDATE imported_properties SET status = :newStatus, reviewedAt = :reviewedAt, reviewedBy = :reviewedBy, reviewNotes = :notes WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String, reviewedAt: Long, reviewedBy: String, notes: String)

    @Query("DELETE FROM imported_properties WHERE id = :id")
    suspend fun delete(id: Long)
}
