package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUserDao {
    @Query("SELECT * FROM app_users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<AppUserEntity>>

    @Query("SELECT * FROM app_users WHERE id = :id")
    suspend fun getUserById(id: Long): AppUserEntity?

    @Query("SELECT * FROM app_users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): AppUserEntity?

    @Query("SELECT COUNT(*) FROM app_users")
    fun getUserCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<AppUserEntity>)

    @Update
    suspend fun updateUser(user: AppUserEntity)

    @Query("UPDATE app_users SET isBlocked = :isBlocked WHERE id = :id")
    suspend fun setUserBlocked(id: Long, isBlocked: Boolean)

    @Query("UPDATE app_users SET role = :role WHERE id = :id")
    suspend fun updateUserRole(id: Long, role: String)

    @Query("DELETE FROM app_users WHERE id = :id")
    suspend fun deleteUser(id: Long)
}
