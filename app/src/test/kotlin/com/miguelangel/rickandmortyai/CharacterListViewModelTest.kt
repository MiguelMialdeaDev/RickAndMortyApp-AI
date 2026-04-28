package com.miguelangel.rickandmortyai

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.CharacterStatus
import com.miguelangel.rickandmortyai.domain.model.Gender
import com.miguelangel.rickandmortyai.domain.usecase.GetCharactersUseCase
import com.miguelangel.rickandmortyai.ui.list.CharacterListViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCharacters: GetCharactersUseCase = mockk()

    @Before
    fun setUpDefaultStub() {
        every { getCharacters(any()) } returns flowOf(PagingData.empty())
    }

    @Test
    fun `query state is empty initially`() {
        val viewModel = CharacterListViewModel(getCharacters)

        assertThat(viewModel.query.value).isEqualTo("")
    }

    @Test
    fun `query state updates immediately on onQueryChange`() {
        val viewModel = CharacterListViewModel(getCharacters)
        viewModel.onQueryChange("morty")

        assertThat(viewModel.query.value).isEqualTo("morty")
    }

    @Test
    fun `onClearQuery resets query to empty`() {
        val viewModel = CharacterListViewModel(getCharacters)
        viewModel.onQueryChange("rick")
        viewModel.onClearQuery()

        assertThat(viewModel.query.value).isEqualTo("")
    }

    @Test
    fun `exposes paging flow with characters from use case for initial empty query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getCharacters("") } returns flowOf(
                PagingData.from((1..3).map { sampleCharacter(it) }),
            )

            val viewModel = CharacterListViewModel(getCharacters)
            advanceUntilIdle()
            val snapshot = viewModel.characters.asSnapshot()

            assertThat(snapshot.map { it.id }).containsExactly(1, 2, 3).inOrder()
        }

    @Test
    fun `exposes empty paging when use case returns empty data`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            advanceUntilIdle()
            val snapshot = viewModel.characters.asSnapshot()

            assertThat(snapshot).isEmpty()
        }

    @Test
    fun `paging flow refetches after debounce when query changes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getCharacters("rick") } returns flowOf(
                PagingData.from(listOf(sampleCharacter(1).copy(name = "Rick"))),
            )

            val viewModel = CharacterListViewModel(getCharacters)
            viewModel.onQueryChange("rick")
            advanceUntilIdle()

            val snapshot = viewModel.characters.asSnapshot()

            assertThat(snapshot.map { it.name }).containsExactly("Rick")
            verify { getCharacters("rick") }
        }

    @Test
    fun `trims whitespace before forwarding query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getCharacters("rick") } returns flowOf(
                PagingData.from(listOf(sampleCharacter(1).copy(name = "Rick"))),
            )

            val viewModel = CharacterListViewModel(getCharacters)
            viewModel.onQueryChange("  rick  ")
            advanceUntilIdle()

            viewModel.characters.asSnapshot()

            verify { getCharacters("rick") }
            verify(exactly = 0) { getCharacters("  rick  ") }
        }

    @Test
    fun `does not refetch when same trimmed query is set twice`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            // Subscribe early so distinctUntilChanged sees every emission as it happens
            val collectJob = launchCharactersCollector(viewModel)

            viewModel.onQueryChange("rick")
            advanceUntilIdle()
            viewModel.onQueryChange("rick")
            advanceUntilIdle()
            viewModel.onQueryChange("rick ")
            advanceUntilIdle()

            collectJob.cancel()

            verify(exactly = 1) { getCharacters("rick") }
        }

    @Test
    fun `rapid keystrokes within debounce window only fire one request`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            val collectJob = launchCharactersCollector(viewModel)

            viewModel.onQueryChange("r")
            advanceTimeBy(100L)
            viewModel.onQueryChange("ri")
            advanceTimeBy(100L)
            viewModel.onQueryChange("ric")
            advanceTimeBy(100L)
            viewModel.onQueryChange("rick")
            advanceUntilIdle()

            collectJob.cancel()

            verify(exactly = 0) { getCharacters("r") }
            verify(exactly = 0) { getCharacters("ri") }
            verify(exactly = 0) { getCharacters("ric") }
            verify(exactly = 1) { getCharacters("rick") }
        }

    @Test
    fun `clearing the query has no debounce delay`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            val collectJob = launchCharactersCollector(viewModel)

            viewModel.onQueryChange("rick")
            advanceUntilIdle()
            viewModel.onClearQuery()
            advanceTimeBy(50L)
            advanceUntilIdle()

            collectJob.cancel()

            verify(atLeast = 2) { getCharacters("") }
        }

    private fun TestScope.launchCharactersCollector(
        viewModel: CharacterListViewModel,
    ) = backgroundScope.launch { viewModel.characters.collect { } }

    private fun sampleCharacter(id: Int) = Character(
        id = id,
        name = "Character $id",
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

