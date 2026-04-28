package com.miguelangel.rickandmortyai.data.mapper

import com.miguelangel.rickandmortyai.data.remote.dto.CharacterDto
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.CharacterStatus
import com.miguelangel.rickandmortyai.domain.model.Gender

internal fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    status = CharacterStatus.from(status),
    species = species,
    type = type,
    gender = Gender.from(gender),
    origin = origin.name,
    location = location.name,
    imageUrl = image,
    episodeIds = episode.mapNotNull { url -> url.substringAfterLast('/').toIntOrNull() },
)
