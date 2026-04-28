package com.miguelangel.rickandmortyai.domain.usecase

import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.repository.CharacterRepository
import javax.inject.Inject

class GetCharacterDetailUseCase @Inject constructor(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(id: Int): Character = repository.getCharacter(id)
}
