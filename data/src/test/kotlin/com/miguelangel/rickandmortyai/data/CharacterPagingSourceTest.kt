package com.miguelangel.rickandmortyai.data

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.data.paging.CharacterPagingSource
import com.miguelangel.rickandmortyai.data.remote.RickAndMortyApi
import com.miguelangel.rickandmortyai.data.remote.dto.CharacterDto
import com.miguelangel.rickandmortyai.data.remote.dto.InfoDto
import com.miguelangel.rickandmortyai.data.remote.dto.PagedResponseDto
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.CharacterStatus
import com.miguelangel.rickandmortyai.domain.model.Gender
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
        val result = source.load(refreshParams())

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
        val result = source.load(appendParams(key = 42))

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
        source.load(refreshParams())

        coVerify(exactly = 1) { api.getCharacters(page = 1, name = "rick") }
    }

    @Test
    fun `load passes null name when query is blank`() = runTest {
        val response = PagedResponseDto<CharacterDto>(
            info = InfoDto(next = null),
            results = emptyList(),
        )
        coEvery { api.getCharacters(page = 1, name = null) } returns response

        val source = CharacterPagingSource(api, query = "   ")
        source.load(refreshParams())

        coVerify(exactly = 1) { api.getCharacters(page = 1, name = null) }
    }

    @Test
    fun `load returns empty Page on 404 (no search matches)`() = runTest {
        coEvery { api.getCharacters(page = 1, name = "zzzz") } throws httpError(404)

        val source = CharacterPagingSource(api, query = "zzzz")
        val result = source.load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).isEmpty()
        assertThat(page.nextKey).isNull()
        assertThat(page.prevKey).isNull()
    }

    @Test
    fun `load returns empty Page on 404 during append with prev key preserved`() = runTest {
        coEvery { api.getCharacters(page = 5, name = "zzzz") } throws httpError(404)

        val source = CharacterPagingSource(api, query = "zzzz")
        val result = source.load(appendParams(key = 5))

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).isEmpty()
        assertThat(page.prevKey).isEqualTo(4)
        assertThat(page.nextKey).isNull()
    }

    @Test
    fun `load returns Error on 500 HttpException`() = runTest {
        coEvery { api.getCharacters(page = 1, name = null) } throws httpError(500)

        val source = CharacterPagingSource(api, query = "")
        val result = source.load(refreshParams())

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
    }

    @Test
    fun `load returns Error on 429 HttpException`() = runTest {
        coEvery { api.getCharacters(page = 1, name = null) } throws httpError(429)

        val source = CharacterPagingSource(api, query = "")
        val result = source.load(refreshParams())

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
    }

    @Test
    fun `load returns Error on IOException`() = runTest {
        coEvery { api.getCharacters(page = any(), name = any()) } throws IOException("offline")

        val source = CharacterPagingSource(api, query = "")
        val result = source.load(refreshParams())

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
    }

    @Test
    fun `getRefreshKey returns null when there is no anchor position`() {
        val source = CharacterPagingSource(api, query = "")
        val state = PagingState<Int, Character>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0,
        )

        assertThat(source.getRefreshKey(state)).isNull()
    }

    @Test
    fun `getRefreshKey returns prevKey + 1 when anchor sits in a page with prevKey`() {
        val source = CharacterPagingSource(api, query = "")
        val page = PagingSource.LoadResult.Page(
            data = (1..10).map { sampleCharacter(it) },
            prevKey = 1,
            nextKey = 3,
        )
        val state = PagingState(
            pages = listOf(page),
            anchorPosition = 5,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0,
        )

        assertThat(source.getRefreshKey(state)).isEqualTo(2)
    }

    @Test
    fun `getRefreshKey falls back to nextKey - 1 when prevKey is null`() {
        val source = CharacterPagingSource(api, query = "")
        val page = PagingSource.LoadResult.Page(
            data = (1..10).map { sampleCharacter(it) },
            prevKey = null,
            nextKey = 2,
        )
        val state = PagingState(
            pages = listOf(page),
            anchorPosition = 0,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0,
        )

        assertThat(source.getRefreshKey(state)).isEqualTo(1)
    }

    private fun refreshParams() = PagingSource.LoadParams.Refresh<Int>(
        key = null, loadSize = 10, placeholdersEnabled = false,
    )

    private fun appendParams(key: Int) = PagingSource.LoadParams.Append<Int>(
        key = key, loadSize = 10, placeholdersEnabled = false,
    )

    private fun httpError(code: Int): HttpException = HttpException(
        Response.error<PagedResponseDto<CharacterDto>>(
            code,
            "{}".toResponseBody("application/json".toMediaType()),
        ),
    )

    private fun sampleCharacter(id: Int) = Character(
        id = id,
        name = "C$id",
        status = CharacterStatus.ALIVE,
        species = "Human",
        type = "",
        gender = Gender.MALE,
        origin = "Earth",
        location = "Earth",
        imageUrl = "",
        episodeIds = emptyList(),
    )
}
