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
import javax.inject.Provider

internal class CharacterRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
    private val pagingSourceProvider: Provider<CharacterPagingSource>,
) : CharacterRepository {

    override fun getCharacters(): Flow<PagingData<Character>> = Pager(
        config = PagingConfig(
            pageSize = UI_PAGE_SIZE,
            prefetchDistance = UI_PAGE_SIZE / 2,
            enablePlaceholders = false,
            initialLoadSize = UI_PAGE_SIZE * 2,
        ),
        pagingSourceFactory = { pagingSourceProvider.get() },
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
        // UI muestra 10 por página. La API devuelve 20 fijos por página de red.
        // Paging 3 trocea internamente lo que devuelve el PagingSource en bloques
        // de pageSize para entregarlos a la UI.
        const val UI_PAGE_SIZE = 10
    }
}
