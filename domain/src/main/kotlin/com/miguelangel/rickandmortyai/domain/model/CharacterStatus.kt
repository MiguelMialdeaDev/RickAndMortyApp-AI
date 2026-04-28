package com.miguelangel.rickandmortyai.domain.model

enum class CharacterStatus {
    ALIVE,
    DEAD,
    UNKNOWN;

    companion object {
        fun from(raw: String?): CharacterStatus = when (raw?.lowercase()) {
            "alive" -> ALIVE
            "dead" -> DEAD
            else -> UNKNOWN
        }
    }
}
