package com.miguelangel.rickandmortyai.domain

import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.repository.CharacterRepository
import com.miguelangel.rickandmortyai.domain.usecase.GetCharactersUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetCharactersUseCaseTest {

    private val repository: CharacterRepository = mockk()
    private val useCase = GetCharactersUseCase(repository)

    @Test
    fun `delegates to repository getCharacters with empty query by default`() = runTest {
        val expected: Flow<PagingData<Character>> = flowOf(PagingData.empty())
        every { repository.getCharacters("") } returns expected

        val result = useCase()

        assertThat(result).isSameInstanceAs(expected)
        verify(exactly = 1) { repository.getCharacters("") }
    }

    @Test
    fun `forwards query to repository`() = runTest {
        val expected: Flow<PagingData<Character>> = flowOf(PagingData.empty())
        every { repository.getCharacters("rick") } returns expected

        val result = useCase("rick")

        assertThat(result).isSameInstanceAs(expected)
        verify(exactly = 1) { repository.getCharacters("rick") }
    }
}
