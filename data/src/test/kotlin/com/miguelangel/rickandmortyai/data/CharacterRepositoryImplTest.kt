package com.miguelangel.rickandmortyai.data

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.data.paging.CharacterPagingSource
import com.miguelangel.rickandmortyai.data.remote.RickAndMortyApi
import com.miguelangel.rickandmortyai.data.remote.dto.CharacterDto
import com.miguelangel.rickandmortyai.data.remote.dto.EpisodeDto
import com.miguelangel.rickandmortyai.data.remote.dto.InfoDto
import com.miguelangel.rickandmortyai.data.remote.dto.PagedResponseDto
import com.miguelangel.rickandmortyai.data.repository.CharacterRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import javax.inject.Provider

class CharacterRepositoryImplTest {

    private val api: RickAndMortyApi = mockk()
    private val pagingSourceProvider = Provider<CharacterPagingSource> { CharacterPagingSource(api) }
    private val repository = CharacterRepositoryImpl(api, pagingSourceProvider)

    @Test
    fun `getCharacter delegates to api and maps to domain`() = runTest {
        coEvery { api.getCharacter(1) } returns CharacterDto(id = 1, name = "Rick", status = "Alive")

        val result = repository.getCharacter(1)

        assertThat(result.id).isEqualTo(1)
        assertThat(result.name).isEqualTo("Rick")
    }

    @Test
    fun `getEpisodes returns empty list without calling api when ids is empty`() = runTest {
        val result = repository.getEpisodes(emptyList())

        assertThat(result).isEmpty()
        coVerify(exactly = 0) { api.getEpisode(any()) }
        coVerify(exactly = 0) { api.getEpisodes(any()) }
    }

    @Test
    fun `getEpisodes calls single endpoint when only one id is given`() = runTest {
        coEvery { api.getEpisode(5) } returns EpisodeDto(id = 5, name = "Ep5", airDate = "2014", episode = "S01E05")

        val result = repository.getEpisodes(listOf(5))

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(5)
        coVerify(exactly = 1) { api.getEpisode(5) }
        coVerify(exactly = 0) { api.getEpisodes(any()) }
    }

    @Test
    fun `getEpisodes calls bulk endpoint with comma joined ids when multiple given`() = runTest {
        coEvery { api.getEpisodes("1,2,3") } returns listOf(
            EpisodeDto(id = 1),
            EpisodeDto(id = 2),
            EpisodeDto(id = 3),
        )

        val result = repository.getEpisodes(listOf(1, 2, 3))

        assertThat(result.map { it.id }).containsExactly(1, 2, 3).inOrder()
        coVerify(exactly = 1) { api.getEpisodes("1,2,3") }
        coVerify(exactly = 0) { api.getEpisode(any()) }
    }

    @Test
    fun `getCharacters emits items from paging in UI page sizes of 10`() = runTest {
        coEvery { api.getCharacters(page = 1) } returns PagedResponseDto(
            info = InfoDto(next = "https://rickandmortyapi.com/api/character?page=2"),
            results = (1..20).map { CharacterDto(id = it, name = "C$it") },
        )
        coEvery { api.getCharacters(page = 2) } returns PagedResponseDto(
            info = InfoDto(next = null),
            results = (21..40).map { CharacterDto(id = it, name = "C$it") },
        )

        val flow: kotlinx.coroutines.flow.Flow<PagingData<com.miguelangel.rickandmortyai.domain.model.Character>> =
            repository.getCharacters()

        val snapshot = flow.asSnapshot { scrollTo(index = 25) }

        assertThat(snapshot).hasSize(40)
        assertThat(snapshot.first().id).isEqualTo(1)
        assertThat(snapshot.last().id).isEqualTo(40)
    }
}
