package com.miguelangel.rickandmortyai.domain

import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.domain.model.Episode
import com.miguelangel.rickandmortyai.domain.repository.CharacterRepository
import com.miguelangel.rickandmortyai.domain.usecase.GetEpisodesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetEpisodesUseCaseTest {

    private val repository: CharacterRepository = mockk(relaxed = true)
    private val useCase = GetEpisodesUseCase(repository)

    @Test
    fun `returns empty list without hitting repository when ids is empty`() = runTest {
        val result = useCase(emptyList())

        assertThat(result).isEmpty()
        coVerify(exactly = 0) { repository.getEpisodes(any()) }
    }

    @Test
    fun `delegates to repository when ids is not empty`() = runTest {
        val expected = listOf(Episode(id = 1, name = "Pilot", airDate = "December 2, 2013", code = "S01E01"))
        coEvery { repository.getEpisodes(listOf(1)) } returns expected

        val result = useCase(listOf(1))

        assertThat(result).isEqualTo(expected)
    }
}
