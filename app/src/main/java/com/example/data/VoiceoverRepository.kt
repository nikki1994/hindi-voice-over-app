package com.example.data

import kotlinx.coroutines.flow.Flow

class VoiceoverRepository(private val dao: VoiceoverDao) {
    val allVoiceovers: Flow<List<VoiceoverEntity>> = dao.getAllVoiceovers()
    val favoriteVoiceovers: Flow<List<VoiceoverEntity>> = dao.getFavoriteVoiceovers()

    suspend fun getById(id: Long): VoiceoverEntity? = dao.getVoiceoverById(id)

    suspend fun insert(entity: VoiceoverEntity): Long = dao.insertVoiceover(entity)

    suspend fun update(entity: VoiceoverEntity) = dao.updateVoiceover(entity)

    suspend fun toggleFavorite(entity: VoiceoverEntity) {
        dao.updateVoiceover(entity.copy(isFavorite = !entity.isFavorite))
    }

    suspend fun delete(entity: VoiceoverEntity) = dao.deleteVoiceover(entity)

    suspend fun deleteById(id: Long) = dao.deleteVoiceoverById(id)
}
