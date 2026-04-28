package com.miguelangel.rickandmortyai.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.miguelangel.rickandmortyai.data.mapper.toDomain
import com.miguelangel.rickandmortyai.data.remote.RickAndMortyApi
import com.miguelangel.rickandmortyai.domain.model.Character
import retrofit2.HttpException
import java.io.IOException

internal class CharacterPagingSource(
    private val api: RickAndMortyApi,
    private val query: String,
) : PagingSource<Int, Character>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        val apiPage = params.key ?: STARTING_API_PAGE
        val nameFilter = query.trim().takeIf { it.isNotEmpty() }
        return try {
            val response = api.getCharacters(page = apiPage, name = nameFilter)
            LoadResult.Page(
                data = response.results.map { it.toDomain() },
                prevKey = if (apiPage == STARTING_API_PAGE) null else apiPage - 1,
                nextKey = if (response.info.next == null) null else apiPage + 1,
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            if (e.code() == HTTP_NOT_FOUND) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (apiPage == STARTING_API_PAGE) null else apiPage - 1,
                    nextKey = null,
                )
            } else {
                LoadResult.Error(e)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    private companion object {
        const val STARTING_API_PAGE = 1
        const val HTTP_NOT_FOUND = 404
    }
}
