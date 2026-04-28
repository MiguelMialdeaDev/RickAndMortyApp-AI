package com.miguelangel.rickandmortyai.domain

import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.CharacterStatus
import com.miguelangel.rickandmortyai.domain.model.Gender
import com.miguelangel.rickandmortyai.domain.repository.CharacterRepository
import com.miguelangel.rickandmortyai.domain.usecase.GetCharacterDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetCharacterDetailUseCaseTest {

    private val repository: CharacterRepository = mockk()
    private val useCase = GetCharacterDetailUseCase(repository)

    @Test
    fun `returns character from repository`() = runTest {
        val character = Character(
            id = 7,
            name = "Birdperson",
            status = CharacterStatus.ALIVE,
            species = "Bird-Person",
            type = "",
            gender = Gender.MALE,
            origin = "Bird World",
            location = "Bird World",
            imageUrl = "https://example.com/7.png",
            episodeIds = listOf(11, 15, 18),
        )
        coEvery { repository.getCharacter(7) } returns character

        val result = useCase(7)

        assertThat(result).isEqualTo(character)
        coVerify(exactly = 1) { repository.getCharacter(7) }
    }

    @Test(expected = IllegalStateException::class)
    fun `propagates exception from repository`() = runTest {
        coEvery { repository.getCharacter(any()) } throws IllegalStateException("boom")

        useCase(1)
    }
}
