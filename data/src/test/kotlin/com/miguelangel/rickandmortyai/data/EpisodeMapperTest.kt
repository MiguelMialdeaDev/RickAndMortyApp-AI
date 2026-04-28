package com.miguelangel.rickandmortyai.data

import com.google.common.truth.Truth.assertThat
import com.miguelangel.rickandmortyai.data.mapper.toDomain
import com.miguelangel.rickandmortyai.data.remote.dto.EpisodeDto
import org.junit.Test

class EpisodeMapperTest {

    @Test
    fun `maps DTO to domain preserving all fields`() {
        val dto = EpisodeDto(
            id = 1,
            name = "Pilot",
            airDate = "December 2, 2013",
            episode = "S01E01",
        )

        val episode = dto.toDomain()

        assertThat(episode.id).isEqualTo(1)
        assertThat(episode.name).isEqualTo("Pilot")
        assertThat(episode.airDate).isEqualTo("December 2, 2013")
        assertThat(episode.code).isEqualTo("S01E01")
    }

    @Test
    fun `maps DTO with empty defaults`() {
        val dto = EpisodeDto(id = 42)

        val episode = dto.toDomain()

        assertThat(episode.id).isEqualTo(42)
        assertThat(episode.name).isEmpty()
        assertThat(episode.airDate).isEmpty()
        assertThat(episode.code).isEmpty()
    }

    @Test
    fun `code field comes from episode field of DTO`() {
        val dto = EpisodeDto(id = 28, name = "The Ricklantis Mixup", airDate = "September 10, 2017", episode = "S03E07")

        val episode = dto.toDomain()

        assertThat(episode.code).isEqualTo("S03E07")
        assertThat(episode.name).isEqualTo("The Ricklantis Mixup")
    }
}
