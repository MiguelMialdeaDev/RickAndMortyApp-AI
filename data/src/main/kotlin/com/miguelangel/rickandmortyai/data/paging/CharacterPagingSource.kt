package com.miguelangel.rickandmortyai.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.miguelangel.rickandmortyai.data.mapper.toDomain
import com.miguelangel.rickandmortyai.data.remote.RickAndMortyApi
import com.miguelangel.rickandmortyai.domain.model.Character
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CharacterPagingSource @Inject constructor(
    private val api: RickAndMortyApi,
) : PagingSource<Int, Character>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        val apiPage = params.key ?: STARTING_API_PAGE
        return try {
            val response = api.getCharacters(page = apiPage)
            LoadResult.Page(
                data = response.results.map { it.toDomain() },
                prevKey = if (apiPage == STARTING_API_PAGE) null else apiPage - 1,
                nextKey = if (response.info.next == null) null else apiPage + 1,
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
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
    }
}
