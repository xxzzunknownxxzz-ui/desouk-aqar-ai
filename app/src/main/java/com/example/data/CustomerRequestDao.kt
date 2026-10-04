package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerRequestDao {
    @Query("SELECT * FROM customer_requests WHERE isArchived = 0 ORDER BY timestamp DESC")
    fun getAllActiveRequests(): Flow<List<CustomerRequestEntity>>

    @Query("SELECT * FROM customer_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<CustomerRequestEntity>>

    @Query("SELECT COUNT(*) FROM customer_requests WHERE status = 'جديد' AND isArchived = 0")
    fun getNewRequestsCount(): Flow<Int>

    @Query("SELECT * FROM customer_requests WHERE id = :id")
    fun getRequestById(id: Long): Flow<CustomerRequestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: CustomerRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<CustomerRequestEntity>)

    @Update
    suspend fun updateRequest(request: CustomerRequestEntity)

    @Query("UPDATE customer_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Long, status: String)

    @Query("UPDATE customer_requests SET isArchived = 1 WHERE id = :id")
    suspend fun archiveRequest(id: Long)

    @Query("DELETE FROM customer_requests WHERE id = :id")
    suspend fun deleteRequest(id: Long)
}
