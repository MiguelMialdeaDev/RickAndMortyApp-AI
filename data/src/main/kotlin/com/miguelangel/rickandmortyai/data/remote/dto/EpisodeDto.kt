package com.miguelangel.rickandmortyai.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeDto(
    val id: Int,
    val name: String = "",
    @SerialName("air_date") val airDate: String = "",
    val episode: String = "",
)
