package com.miguelangel.rickandmortyai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InfoDto(
    val count: Int = 0,
    val pages: Int = 0,
    val next: String? = null,
    val prev: String? = null,
)
