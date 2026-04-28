package com.miguelangel.rickandmortyai.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.usecase.GetCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    getCharacters: GetCharactersUseCase,
) : ViewModel() {

    val characters: Flow<PagingData<Character>> = getCharacters().cachedIn(viewModelScope)
}
