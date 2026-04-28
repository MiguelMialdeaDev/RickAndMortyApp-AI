package com.miguelangel.rickandmortyai.data.di

import com.miguelangel.rickandmortyai.data.repository.CharacterRepositoryImpl
import com.miguelangel.rickandmortyai.domain.repository.CharacterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository
}
