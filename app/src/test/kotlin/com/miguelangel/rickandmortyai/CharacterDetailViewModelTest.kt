package com.miguelangel.rickandmortyai

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.CharacterStatus
import com.miguelangel.rickandmortyai.domain.model.Episode
import com.miguelangel.rickandmortyai.domain.model.Gender
import com.miguelangel.rickandmortyai.domain.usecase.GetCharacterDetailUseCase
import com.miguelangel.rickandmortyai.domain.usecase.GetEpisodesUseCase
import com.miguelangel.rickandmortyai.ui.detail.CharacterDetailUiState
import com.miguelangel.rickandmortyai.ui.detail.CharacterDetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val getCharacterDetail: GetCharacterDetailUseCase = mockk()
    private val getEpisodes: GetEpisodesUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits Loading then Success when use cases succeed`() = runTest(dispatcher) {
        val character = sampleCharacter()
        val episodes = listOf(Episode(1, "Pilot", "Dec 2, 2013", "S01E01"))
        coEvery { getCharacterDetail(1) } returns character
        coEvery { getEpisodes(listOf(1)) } returns episodes

        val viewModel = CharacterDetailViewModel(savedStateForId(1), getCharacterDetail, getEpisodes)

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(CharacterDetailUiState.Loading)
            dispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem() as CharacterDetailUiState.Success
            assertThat(success.character).isEqualTo(character)
            assertThat(success.episodes).isEqualTo(episodes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Error when character fetch fails`() = runTest(dispatcher) {
        coEvery { getCharacterDetail(1) } throws IllegalStateException("boom")

        val viewModel = CharacterDetailViewModel(savedStateForId(1), getCharacterDetail, getEpisodes)

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(CharacterDetailUiState.Loading)
            dispatcher.scheduler.advanceUntilIdle()
            val error = awaitItem()
            assertThat(error).isInstanceOf(CharacterDetailUiState.Error::class.java)
            assertThat((error as CharacterDetailUiState.Error).message).isEqualTo("boom")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Success with empty episodes when episodes fetch fails`() = runTest(dispatcher) {
        val character = sampleCharacter()
        coEvery { getCharacterDetail(1) } returns character
        coEvery { getEpisodes(any()) } throws IllegalStateException("episodes-down")

        val viewModel = CharacterDetailViewModel(savedStateForId(1), getCharacterDetail, getEpisodes)

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(CharacterDetailUiState.Loading)
            dispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem() as CharacterDetailUiState.Success
            assertThat(success.character).isEqualTo(character)
            assertThat(success.episodes).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not call getEpisodes when character has no episodes`() = runTest(dispatcher) {
        val character = sampleCharacter().copy(episodeIds = emptyList())
        coEvery { getCharacterDetail(1) } returns character
        coEvery { getEpisodes(emptyList()) } returns emptyList()

        val viewModel = CharacterDetailViewModel(savedStateForId(1), getCharacterDetail, getEpisodes)

        viewModel.state.test {
            awaitItem() // Loading
            dispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem() as CharacterDetailUiState.Success
            assertThat(success.episodes).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry resets to Loading and refetches`() = runTest(dispatcher) {
        val character = sampleCharacter()
        coEvery { getCharacterDetail(1) } throws IllegalStateException("boom") andThen character
        coEvery { getEpisodes(any()) } returns emptyList()

        val viewModel = CharacterDetailViewModel(savedStateForId(1), getCharacterDetail, getEpisodes)

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(CharacterDetailUiState.Loading)
            dispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem()).isInstanceOf(CharacterDetailUiState.Error::class.java)

            viewModel.retry()
            assertThat(awaitItem()).isEqualTo(CharacterDetailUiState.Loading)
            dispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem() as CharacterDetailUiState.Success
            assertThat(success.character).isEqualTo(character)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { getCharacterDetail(1) }
    }

    private fun savedStateForId(id: Int) = SavedStateHandle(mapOf("id" to id))

    private fun sampleCharacter() = Character(
        id = 1,
        name = "Rick Sanchez",
        status = CharacterStatus.ALIVE,
        species = "Human",
        type = "",
        gender = Gender.MALE,
        origin = "Earth",
        location = "Citadel",
        imageUrl = "",
        episodeIds = listOf(1),
    )
}
