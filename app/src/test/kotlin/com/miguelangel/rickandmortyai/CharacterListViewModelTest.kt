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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val getCharacters: GetCharactersUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exposes paging flow with characters from use case for initial empty query`() = runTest(dispatcher) {
        val data = (1..3).map { sampleCharacter(it) }
        every { getCharacters("") } returns flowOf(PagingData.from(data))

        val viewModel = CharacterListViewModel(getCharacters)

        val snapshot = viewModel.characters.asSnapshot()

        assertThat(snapshot.map { it.id }).containsExactly(1, 2, 3).inOrder()
    }

    @Test
    fun `exposes empty paging when use case returns empty data`() = runTest(dispatcher) {
        every { getCharacters("") } returns flowOf(PagingData.empty())

        val viewModel = CharacterListViewModel(getCharacters)

        val snapshot = viewModel.characters.asSnapshot()

        assertThat(snapshot).isEmpty()
    }

    @Test
    fun `query state updates immediately on onQueryChange`() = runTest(dispatcher) {
        every { getCharacters(any()) } returns flowOf(PagingData.empty())

        val viewModel = CharacterListViewModel(getCharacters)
        viewModel.onQueryChange("morty")

        assertThat(viewModel.query.value).isEqualTo("morty")
    }

    @Test
    fun `onClearQuery resets query to empty`() = runTest(dispatcher) {
        every { getCharacters(any()) } returns flowOf(PagingData.empty())

        val viewModel = CharacterListViewModel(getCharacters)
        viewModel.onQueryChange("rick")
        viewModel.onClearQuery()

        assertThat(viewModel.query.value).isEqualTo("")
    }

    @Test
    fun `paging flow refetches after debounce when query changes`() = runTest(dispatcher) {
        every { getCharacters("") } returns flowOf(PagingData.empty())
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
    fun `trims whitespace before forwarding query`() = runTest(dispatcher) {
        every { getCharacters("") } returns flowOf(PagingData.empty())
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
    fun `does not refetch when same trimmed query is set twice`() = runTest(dispatcher) {
        every { getCharacters("") } returns flowOf(PagingData.empty())
        every { getCharacters("rick") } returns flowOf(PagingData.empty())

        val viewModel = CharacterListViewModel(getCharacters)
        viewModel.onQueryChange("rick")
        advanceUntilIdle()
        viewModel.onQueryChange("rick")
        advanceUntilIdle()
        viewModel.onQueryChange("rick ")
        advanceUntilIdle()

        viewModel.characters.asSnapshot()

        verify(exactly = 1) { getCharacters("rick") }
    }

    @Test
    fun `rapid keystrokes within debounce window only fire one request`() = runTest(dispatcher) {
        every { getCharacters("") } returns flowOf(PagingData.empty())
        every { getCharacters(any()) } returns flowOf(PagingData.empty())

        val viewModel = CharacterListViewModel(getCharacters)
        viewModel.onQueryChange("r")
        advanceTimeBy(100L)
        viewModel.onQueryChange("ri")
        advanceTimeBy(100L)
        viewModel.onQueryChange("ric")
        advanceTimeBy(100L)
        viewModel.onQueryChange("rick")
        advanceUntilIdle()

        viewModel.characters.asSnapshot()

        verify(exactly = 0) { getCharacters("r") }
        verify(exactly = 0) { getCharacters("ri") }
        verify(exactly = 0) { getCharacters("ric") }
        verify(exactly = 1) { getCharacters("rick") }
    }

    @Test
    fun `clearing the query has no debounce delay`() = runTest(dispatcher) {
        every { getCharacters("") } returns flowOf(PagingData.empty())
        every { getCharacters("rick") } returns flowOf(PagingData.empty())

        val viewModel = CharacterListViewModel(getCharacters)
        viewModel.onQueryChange("rick")
        advanceUntilIdle()
        viewModel.onClearQuery()
        // Without advancing past 600 ms, the empty query should still propagate
        advanceTimeBy(50L)
        advanceUntilIdle()

        viewModel.characters.asSnapshot()

        verify(atLeast = 2) { getCharacters("") }
    }

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
