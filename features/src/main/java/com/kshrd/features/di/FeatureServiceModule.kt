package com.kshrd.features.di

import com.kshrd.features.note.data.service.NoteServiceImpl
import com.kshrd.features.note.domain.service.NoteService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Interface-to-implementation bindings for services. `@Binds` needs no body. */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureServiceModule {
    @Binds
    @Singleton
    abstract fun bindNoteService(impl: NoteServiceImpl): NoteService
}
