package com.miguelangel.rickandmortyai.domain.model

data class Character(
    val id: Int,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val type: String,
    val gender: Gender,
    val origin: String,
    val location: String,
    val imageUrl: String,
    val episodeIds: List<Int>,
)
