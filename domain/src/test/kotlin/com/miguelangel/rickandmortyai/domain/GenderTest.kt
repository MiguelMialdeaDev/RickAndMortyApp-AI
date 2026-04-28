package com.miguelangel.rickandmortyai.domain

import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.domain.model.Gender
import org.junit.Test

class GenderTest {

    @Test
    fun `from maps each known value`() {
        assertThat(Gender.from("Female")).isEqualTo(Gender.FEMALE)
        assertThat(Gender.from("male")).isEqualTo(Gender.MALE)
        assertThat(Gender.from("GENDERLESS")).isEqualTo(Gender.GENDERLESS)
    }

    @Test
    fun `from defaults to UNKNOWN for null or unrecognized`() {
        assertThat(Gender.from(null)).isEqualTo(Gender.UNKNOWN)
        assertThat(Gender.from("alien")).isEqualTo(Gender.UNKNOWN)
    }
}
