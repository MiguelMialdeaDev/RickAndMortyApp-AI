package com.miguelangel.rickandmortyai.data

import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.data.paging.CharacterPagingSource
import com.miguelangel.rickandmortyai.data.remote.RickAndMortyApi
import com.miguelangel.rickandmortyai.data.remote.dto.CharacterDto
import com.miguelangel.rickandmortyai.data.remote.dto.InfoDto
import com.miguelangel.rickandmortyai.data.remote.dto.PagedResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class CharacterPagingSourceTest {

    private val api: RickAndMortyApi = mockk()

    @Test
    fun `load returns Page with next key when API has more pages`() = runTest {
        val response = PagedResponseDto(
            info = InfoDto(next = "https://rickandmortyapi.com/api/character?page=2"),
            results = listOf(CharacterDto(id = 1), CharacterDto(id = 2)),
        )
        coEvery { api.getCharacters(page = 1) } returns response

        val source = CharacterPagingSource(api)
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
        coEvery { api.getCharacters(page = 42) } returns response

        val source = CharacterPagingSource(api)
        val result = source.load(PagingSource.LoadParams.Append(key = 42, loadSize = 10, placeholdersEnabled = false))

        val page = result as PagingSource.LoadResult.Page
        assertThat(page.nextKey).isNull()
        assertThat(page.prevKey).isEqualTo(41)
    }

    @Test
    fun `load returns Error on IOException`() = runTest {
        coEvery { api.getCharacters(page = any()) } throws IOException("offline")

        val source = CharacterPagingSource(api)
        val result = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false))

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
    }
}
