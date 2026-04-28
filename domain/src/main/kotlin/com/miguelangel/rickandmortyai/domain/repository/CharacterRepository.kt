package com.miguelangel.rickandmortyai.domain.repository

import androidx.paging.PagingData
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.Episode
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacters(): Flow<PagingData<Character>>
    suspend fun getCharacter(id: Int): Character
    suspend fun getEpisodes(ids: List<Int>): List<Episode>
}
