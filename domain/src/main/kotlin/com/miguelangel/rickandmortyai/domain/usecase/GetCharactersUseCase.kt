package com.miguelangel.rickandmortyai.domain.usecase

import androidx.paging.PagingData
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCharactersUseCase @Inject constructor(
    private val repository: CharacterRepository,
) {
    operator fun invoke(): Flow<PagingData<Character>> = repository.getCharacters()
}
