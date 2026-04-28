package com.miguelangel.rickandmortyai.domain.usecase

import com.miguelangel.rickandmortyai.domain.model.Episode
import com.miguelangel.rickandmortyai.domain.repository.CharacterRepository
import javax.inject.Inject

class GetEpisodesUseCase @Inject constructor(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(ids: List<Int>): List<Episode> {
        if (ids.isEmpty()) return emptyList()
        return repository.getEpisodes(ids)
    }
}
