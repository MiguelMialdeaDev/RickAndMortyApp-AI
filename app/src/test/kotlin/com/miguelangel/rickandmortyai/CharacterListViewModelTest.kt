package com.miguelangel.rickandmortyai

import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.domain.usecase.GetCharactersUseCase
import com.miguelangel.rickandmortyai.ui.list.CharacterListViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
    fun `initial subscription invokes use case with empty query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            val job = collectCharacters(viewModel)
            advanceUntilIdle()

            verify { getCharacters("") }

            job.cancel()
        }

    @Test
    fun `paging flow refetches after debounce when query changes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            val job = collectCharacters(viewModel)

            viewModel.onQueryChange("rick")
            advanceUntilIdle()

            verify { getCharacters("rick") }

            job.cancel()
        }

    @Test
    fun `trims whitespace before forwarding query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            val job = collectCharacters(viewModel)

            viewModel.onQueryChange("  rick  ")
            advanceUntilIdle()

            verify { getCharacters("rick") }
            verify(exactly = 0) { getCharacters("  rick  ") }

            job.cancel()
        }

    @Test
    fun `does not refetch when same trimmed query is set twice`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            val job = collectCharacters(viewModel)

            viewModel.onQueryChange("rick")
            advanceUntilIdle()
            viewModel.onQueryChange("rick")
            advanceUntilIdle()
            viewModel.onQueryChange("rick ")
            advanceUntilIdle()

            verify(exactly = 1) { getCharacters("rick") }

            job.cancel()
        }

    @Test
    fun `rapid keystrokes within debounce window only fire one request`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            val job = collectCharacters(viewModel)

            viewModel.onQueryChange("r")
            advanceTimeBy(100L)
            viewModel.onQueryChange("ri")
            advanceTimeBy(100L)
            viewModel.onQueryChange("ric")
            advanceTimeBy(100L)
            viewModel.onQueryChange("rick")
            advanceUntilIdle()

            verify(exactly = 0) { getCharacters("r") }
            verify(exactly = 0) { getCharacters("ri") }
            verify(exactly = 0) { getCharacters("ric") }
            verify(exactly = 1) { getCharacters("rick") }

            job.cancel()
        }

    @Test
    fun `clearing the query has no debounce delay`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CharacterListViewModel(getCharacters)
            val job = collectCharacters(viewModel)

            viewModel.onQueryChange("rick")
            advanceUntilIdle()
            viewModel.onClearQuery()
            advanceTimeBy(50L)
            advanceUntilIdle()

            verify(atLeast = 2) { getCharacters("") }

            job.cancel()
        }

    private fun TestScope.collectCharacters(viewModel: CharacterListViewModel): Job =
        backgroundScope.launch { viewModel.characters.collect { } }
}
