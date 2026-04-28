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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
    fun `exposes paging flow with characters from use case`() = runTest {
        val data = (1..3).map { sampleCharacter(it) }
        every { getCharacters() } returns flowOf(PagingData.from(data))

        val viewModel = CharacterListViewModel(getCharacters)

        val snapshot = viewModel.characters.asSnapshot()

        assertThat(snapshot.map { it.id }).containsExactly(1, 2, 3).inOrder()
    }

    @Test
    fun `exposes empty paging when use case returns empty data`() = runTest {
        every { getCharacters() } returns flowOf(PagingData.empty())

        val viewModel = CharacterListViewModel(getCharacters)

        val snapshot = viewModel.characters.asSnapshot()

        assertThat(snapshot).isEmpty()
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
