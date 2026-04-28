package com.miguelangel.rickandmortyai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PagedResponseDto<T>(
    val info: InfoDto = InfoDto(),
    val results: List<T> = emptyList(),
)
