package com.miguelangel.rickandmortyai.data.mapper

import com.miguelangel.rickandmortyai.data.remote.dto.EpisodeDto
import com.miguelangel.rickandmortyai.domain.model.Episode

internal fun EpisodeDto.toDomain(): Episode = Episode(
    id = id,
    name = name,
    airDate = airDate,
    code = episode,
)
