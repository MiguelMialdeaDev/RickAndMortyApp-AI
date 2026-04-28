package com.miguelangel.rickandmortyai.domain.model

enum class Gender {
    FEMALE,
    MALE,
    GENDERLESS,
    UNKNOWN;

    companion object {
        fun from(raw: String?): Gender = when (raw?.lowercase()) {
            "female" -> FEMALE
            "male" -> MALE
            "genderless" -> GENDERLESS
            else -> UNKNOWN
        }
    }
}
