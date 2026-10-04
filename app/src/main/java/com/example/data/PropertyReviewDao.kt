package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyReviewDao {
    @Query("SELECT * FROM property_reviews WHERE propertyId = :propertyId ORDER BY timestamp DESC")
    fun getReviewsForProperty(propertyId: Long): Flow<List<PropertyReviewEntity>>

    @Query("SELECT AVG(rating) FROM property_reviews WHERE propertyId = :propertyId")
    fun getAverageRating(propertyId: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM property_reviews WHERE propertyId = :propertyId")
    fun getReviewCount(propertyId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: PropertyReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<PropertyReviewEntity>)

    @Query("SELECT COUNT(*) FROM property_reviews")
    suspend fun getTotalReviewCount(): Int

    @Query("DELETE FROM property_reviews WHERE id = :id")
    suspend fun deleteReview(id: Long)
}
