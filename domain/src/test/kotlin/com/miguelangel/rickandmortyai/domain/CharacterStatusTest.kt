package com.miguelangel.rickandmortyai.domain

import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.domain.model.CharacterStatus
import org.junit.Test

class CharacterStatusTest {

    @Test
    fun `from maps known values regardless of case`() {
        assertThat(CharacterStatus.from("Alive")).isEqualTo(CharacterStatus.ALIVE)
        assertThat(CharacterStatus.from("ALIVE")).isEqualTo(CharacterStatus.ALIVE)
        assertThat(CharacterStatus.from("dead")).isEqualTo(CharacterStatus.DEAD)
    }

    @Test
    fun `from maps null and unknown to UNKNOWN`() {
        assertThat(CharacterStatus.from(null)).isEqualTo(CharacterStatus.UNKNOWN)
        assertThat(CharacterStatus.from("")).isEqualTo(CharacterStatus.UNKNOWN)
        assertThat(CharacterStatus.from("anything-else")).isEqualTo(CharacterStatus.UNKNOWN)
    }
}
