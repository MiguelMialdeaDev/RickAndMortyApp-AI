package com.miguelangel.rickandmortyai.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miguelangel.rickandmortyai.domain.usecase.GetCharacterDetailUseCase
import com.miguelangel.rickandmortyai.domain.usecase.GetEpisodesUseCase
import com.miguelangel.rickandmortyai.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCharacterDetail: GetCharacterDetailUseCase,
    private val getEpisodes: GetEpisodesUseCase,
) : ViewModel() {

    private val characterId: Int = savedStateHandle.toRoute<Route.CharacterDetail>().id

    private val _state = MutableStateFlow<CharacterDetailUiState>(CharacterDetailUiState.Loading)
    val state: StateFlow<CharacterDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _state.value = CharacterDetailUiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val character = getCharacterDetail(characterId)
                val episodes = runCatching { getEpisodes(character.episodeIds) }.getOrDefault(emptyList())
                _state.value = CharacterDetailUiState.Success(character, episodes)
            } catch (e: Exception) {
                _state.value = CharacterDetailUiState.Error(e.message)
            }
        }
    }
}
