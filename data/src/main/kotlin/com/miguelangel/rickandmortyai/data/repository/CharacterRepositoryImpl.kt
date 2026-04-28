package com.miguelangel.rickandmortyai.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.miguelangel.rickandmortyai.data.mapper.toDomain
import com.miguelangel.rickandmortyai.data.paging.CharacterPagingSource
import com.miguelangel.rickandmortyai.data.remote.RickAndMortyApi
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.Episode
import com.miguelangel.rickandmortyai.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class CharacterRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
) : CharacterRepository {

    override fun getCharacters(query: String): Flow<PagingData<Character>> = Pager(
        config = PagingConfig(
            pageSize = UI_PAGE_SIZE,
            prefetchDistance = UI_PAGE_SIZE / 2,
            enablePlaceholders = false,
            initialLoadSize = UI_PAGE_SIZE * 2,
        ),
        pagingSourceFactory = { CharacterPagingSource(api, query) },
    ).flow

    override suspend fun getCharacter(id: Int): Character = api.getCharacter(id).toDomain()

    override suspend fun getEpisodes(ids: List<Int>): List<Episode> {
        return when (ids.size) {
            0 -> emptyList()
            1 -> listOf(api.getEpisode(ids.first()).toDomain())
            else -> api.getEpisodes(ids.joinToString(",")).map { it.toDomain() }
        }
    }

    private companion object {
        const val UI_PAGE_SIZE = 10
    }
}
