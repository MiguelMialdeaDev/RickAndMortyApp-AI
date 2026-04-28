package com.miguelangel.rickandmortyai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String = "",
    val status: String = "",
    val species: String = "",
    val type: String = "",
    val gender: String = "",
    val origin: LocationRefDto = LocationRefDto(),
    val location: LocationRefDto = LocationRefDto(),
    val image: String = "",
    val episode: List<String> = emptyList(),
)

@Serializable
data class LocationRefDto(
    val name: String = "",
    val url: String = "",
)
