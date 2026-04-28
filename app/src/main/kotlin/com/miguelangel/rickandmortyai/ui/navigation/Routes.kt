package com.miguelangel.rickandmortyai.ui.navigation

import kotlinx.serialization.Serializable

internal sealed interface Route {

    @Serializable
    data object CharacterList : Route

    @Serializable
    data class CharacterDetail(val id: Int) : Route
}
