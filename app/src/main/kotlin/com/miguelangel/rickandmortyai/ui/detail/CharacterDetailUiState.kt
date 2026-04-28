package com.miguelangel.rickandmortyai.ui.detail

import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.Episode

sealed interface CharacterDetailUiState {
    data object Loading : CharacterDetailUiState
    data class Success(
        val character: Character,
        val episodes: List<Episode>,
    ) : CharacterDetailUiState
    data class Error(val message: String?) : CharacterDetailUiState
}
