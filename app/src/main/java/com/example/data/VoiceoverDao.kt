package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceoverDao {
    @Query("SELECT * FROM voiceovers ORDER BY timestamp DESC")
    fun getAllVoiceovers(): Flow<List<VoiceoverEntity>>

    @Query("SELECT * FROM voiceovers WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteVoiceovers(): Flow<List<VoiceoverEntity>>

    @Query("SELECT * FROM voiceovers WHERE id = :id")
    suspend fun getVoiceoverById(id: Long): VoiceoverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceover(entity: VoiceoverEntity): Long

    @Update
    suspend fun updateVoiceover(entity: VoiceoverEntity)

    @Delete
    suspend fun deleteVoiceover(entity: VoiceoverEntity)

    @Query("DELETE FROM voiceovers WHERE id = :id")
    suspend fun deleteVoiceoverById(id: Long)
}
