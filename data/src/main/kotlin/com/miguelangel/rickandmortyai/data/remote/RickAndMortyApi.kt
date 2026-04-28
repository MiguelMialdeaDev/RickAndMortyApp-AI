package com.miguelangel.rickandmortyai.data.remote

import com.miguelangel.rickandmortyai.data.remote.dto.CharacterDto
import com.miguelangel.rickandmortyai.data.remote.dto.EpisodeDto
import com.miguelangel.rickandmortyai.data.remote.dto.PagedResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {

    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): PagedResponseDto<CharacterDto>

    @GET("character/{id}")
    suspend fun getCharacter(@Path("id") id: Int): CharacterDto

    @GET("episode/{ids}")
    suspend fun getEpisodes(@Path("ids") ids: String): List<EpisodeDto>

    @GET("episode/{id}")
    suspend fun getEpisode(@Path("id") id: Int): EpisodeDto
}
