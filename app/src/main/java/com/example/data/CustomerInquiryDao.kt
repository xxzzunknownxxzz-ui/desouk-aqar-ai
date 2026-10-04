package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerInquiryDao {
    @Query("SELECT * FROM customer_inquiries ORDER BY timestamp DESC")
    fun getAllInquiries(): Flow<List<CustomerInquiryEntity>>

    @Query("SELECT COUNT(*) FROM customer_inquiries WHERE status = 'جديد'")
    fun getNewInquiriesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customer_inquiries")
    fun getTotalInquiriesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: CustomerInquiryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inquiries: List<CustomerInquiryEntity>)

    @Update
    suspend fun updateInquiry(inquiry: CustomerInquiryEntity)

    @Query("UPDATE customer_inquiries SET status = :status, replyNotes = :replyNotes WHERE id = :id")
    suspend fun replyInquiry(id: Long, status: String, replyNotes: String)

    @Query("DELETE FROM customer_inquiries WHERE id = :id")
    suspend fun deleteInquiry(id: Long)
}
