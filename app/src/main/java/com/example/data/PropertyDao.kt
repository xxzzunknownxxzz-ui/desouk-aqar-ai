package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY timestamp DESC")
    fun getAllProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE isUserAdded = 1 ORDER BY timestamp DESC")
    fun getUserProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(properties: List<PropertyEntity>)

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    @Query("UPDATE properties SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE properties SET status = :status WHERE id = :id")
    suspend fun updatePropertyStatus(id: Long, status: String)

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyByIdDirect(id: Long): PropertyEntity?

    @Query("SELECT * FROM properties")
    suspend fun getAllPropertiesDirect(): List<PropertyEntity>

    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun deleteProperty(id: Long)

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun getCount(): Int
}
