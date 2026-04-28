package com.miguelangel.rickandmortyai.data

import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.data.mapper.toDomain
import com.miguelangel.rickandmortyai.data.remote.dto.CharacterDto
import com.miguelangel.rickandmortyai.data.remote.dto.LocationRefDto
import com.miguelangel.rickandmortyai.domain.model.CharacterStatus
import com.miguelangel.rickandmortyai.domain.model.Gender
import org.junit.Test

class CharacterMapperTest {

    @Test
    fun `maps DTO to domain with parsed status, gender and episode ids`() {
        val dto = CharacterDto(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            origin = LocationRefDto(name = "Earth (C-137)"),
            location = LocationRefDto(name = "Citadel of Ricks"),
            image = "https://example.com/1.png",
            episode = listOf(
                "https://rickandmortyapi.com/api/episode/1",
                "https://rickandmortyapi.com/api/episode/2",
            ),
        )

        val character = dto.toDomain()

        assertThat(character.id).isEqualTo(1)
        assertThat(character.name).isEqualTo("Rick Sanchez")
        assertThat(character.status).isEqualTo(CharacterStatus.ALIVE)
        assertThat(character.gender).isEqualTo(Gender.MALE)
        assertThat(character.origin).isEqualTo("Earth (C-137)")
        assertThat(character.location).isEqualTo("Citadel of Ricks")
        assertThat(character.imageUrl).isEqualTo("https://example.com/1.png")
        assertThat(character.episodeIds).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `unknown status falls back to UNKNOWN`() {
        val dto = CharacterDto(id = 99, status = "weird-value")

        val character = dto.toDomain()

        assertThat(character.status).isEqualTo(CharacterStatus.UNKNOWN)
    }

    @Test
    fun `unknown gender falls back to UNKNOWN`() {
        val dto = CharacterDto(id = 1, gender = "alien")

        val character = dto.toDomain()

        assertThat(character.gender).isEqualTo(Gender.UNKNOWN)
    }

    @Test
    fun `dead status maps to DEAD`() {
        val dto = CharacterDto(id = 1, status = "Dead")

        val character = dto.toDomain()

        assertThat(character.status).isEqualTo(CharacterStatus.DEAD)
    }

    @Test
    fun `genderless gender maps to GENDERLESS`() {
        val dto = CharacterDto(id = 1, gender = "Genderless")

        val character = dto.toDomain()

        assertThat(character.gender).isEqualTo(Gender.GENDERLESS)
    }

    @Test
    fun `episode urls without trailing id are filtered out`() {
        val dto = CharacterDto(
            id = 1,
            episode = listOf(
                "https://rickandmortyapi.com/api/episode/3",
                "https://rickandmortyapi.com/api/episode/abc",
                "",
            ),
        )

        val character = dto.toDomain()

        assertThat(character.episodeIds).containsExactly(3)
    }

    @Test
    fun `empty episode list maps to empty episodeIds`() {
        val dto = CharacterDto(id = 1, episode = emptyList())

        val character = dto.toDomain()

        assertThat(character.episodeIds).isEmpty()
    }

    @Test
    fun `defaults map cleanly when DTO has only required id`() {
        val dto = CharacterDto(id = 7)

        val character = dto.toDomain()

        assertThat(character.id).isEqualTo(7)
        assertThat(character.name).isEmpty()
        assertThat(character.status).isEqualTo(CharacterStatus.UNKNOWN)
        assertThat(character.species).isEmpty()
        assertThat(character.gender).isEqualTo(Gender.UNKNOWN)
        assertThat(character.origin).isEmpty()
        assertThat(character.location).isEmpty()
        assertThat(character.imageUrl).isEmpty()
        assertThat(character.episodeIds).isEmpty()
    }
}
