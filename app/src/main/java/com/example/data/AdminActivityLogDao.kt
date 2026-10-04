package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminActivityLogDao {
    @Query("SELECT * FROM admin_activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AdminActivityLogEntity>>

    @Query("SELECT * FROM admin_activity_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<AdminActivityLogEntity>>

    @Query("SELECT COUNT(*) FROM admin_activity_logs")
    suspend fun getLogCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdminActivityLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<AdminActivityLogEntity>)

    @Query("DELETE FROM admin_activity_logs")
    suspend fun clearAllLogs()
}
