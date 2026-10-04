package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * واجهة DAO لعمليات حفظ واسترجاع ومسح مسودة العقار في Room Database
 */
@Dao
interface PropertyDraftDao {
    @Query("SELECT * FROM property_drafts WHERE id = 1 LIMIT 1")
    fun getDraft(): Flow<PropertyDraftEntity?>

    @Query("SELECT * FROM property_drafts WHERE id = 1 LIMIT 1")
    suspend fun getDraftDirect(): PropertyDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: PropertyDraftEntity)

    @Query("DELETE FROM property_drafts WHERE id = 1")
    suspend fun clearDraft()
}
