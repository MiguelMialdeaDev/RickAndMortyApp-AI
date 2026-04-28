package com.miguelangel.rickandmortyai.data

import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.data.paging.CharacterPagingSource
import com.miguelangel.rickandmortyai.data.remote.RickAndMortyApi
import com.miguelangel.rickandmortyai.data.remote.dto.CharacterDto
import com.miguelangel.rickandmortyai.data.remote.dto.InfoDto
import com.miguelangel.rickandmortyai.data.remote.dto.PagedResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CharacterPagingSourceTest {

    private val api: RickAndMortyApi = mockk()

    @Test
    fun `load returns Page with next key when API has more pages`() = runTest {
        val response = PagedResponseDto(
            info = InfoDto(next = "https://rickandmortyapi.com/api/character?page=2"),
            results = listOf(CharacterDto(id = 1), CharacterDto(id = 2)),
        )
        coEvery { api.getCharacters(page = 1, name = null) } returns response

        val source = CharacterPagingSource(api, query = "")
        val result = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false))

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data.map { it.id }).containsExactly(1, 2).inOrder()
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo(2)
    }

    @Test
    fun `load returns null nextKey when API reports no more pages`() = runTest {
        val response = PagedResponseDto(
            info = InfoDto(next = null),
            results = listOf(CharacterDto(id = 826)),
        )
        coEvery { api.getCharacters(page = 42, name = null) } returns response

        val source = CharacterPagingSource(api, query = "")
        val result = source.load(PagingSource.LoadParams.Append(key = 42, loadSize = 10, placeholdersEnabled = false))

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.nextKey).isNull()
        assertThat(page.prevKey).isEqualTo(41)
    }

    @Test
    fun `load forwards trimmed non-empty query as name filter`() = runTest {
        val response = PagedResponseDto(
            info = InfoDto(next = null),
            results = listOf(CharacterDto(id = 1, name = "Rick Sanchez")),
        )
        coEvery { api.getCharacters(page = 1, name = "rick") } returns response

        val source = CharacterPagingSource(api, query = "  rick  ")
        source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false))

        coVerify(exactly = 1) { api.getCharacters(page = 1, name = "rick") }
    }

    @Test
    fun `load returns empty Page on 404 (no search matches)`() = runTest {
        val notFound = HttpException(
            Response.error<PagedResponseDto<CharacterDto>>(
                404,
                "{\"error\":\"There is nothing here\"}".toResponseBody("application/json".toMediaType()),
            ),
        )
        coEvery { api.getCharacters(page = 1, name = "zzzz") } throws notFound

        val source = CharacterPagingSource(api, query = "zzzz")
        val result = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false))

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).isEmpty()
        assertThat(page.nextKey).isNull()
        assertThat(page.prevKey).isNull()
    }

    @Test
    fun `load returns Error on IOException`() = runTest {
        coEvery { api.getCharacters(page = any(), name = any()) } throws IOException("offline")

        val source = CharacterPagingSource(api, query = "")
        val result = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false))

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
    }
}
