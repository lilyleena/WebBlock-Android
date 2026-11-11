package com.kshrd.features.di

import com.kshrd.features.note.data.api.InMemoryNoteApi
import com.kshrd.features.note.data.api.NoteApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each feature's Retrofit API interface. One `@Provides` per API.
 *
 * The sample uses an in-memory fake so it runs without a server. With a real backend the
 * binding becomes:
 *
 * ```kotlin
 * @Provides @Singleton
 * fun provideNoteApi(retrofit: Retrofit): NoteApi = retrofit.create(NoteApi::class.java)
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object FeatureApiModule {
    @Provides
    @Singleton
    fun provideNoteApi(inMemory: InMemoryNoteApi): NoteApi = inMemory
}
