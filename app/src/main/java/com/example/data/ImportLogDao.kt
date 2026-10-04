package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportLogDao {
    @Query("SELECT * FROM import_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<ImportLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ImportLogEntity): Long

    @Query("DELETE FROM import_logs")
    suspend fun clearLogs()
}
