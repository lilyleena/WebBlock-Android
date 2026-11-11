package com.kshrd.features.di

import com.kshrd.core.session.UnreadCountSource
import com.kshrd.features.auth.data.repository.AuthRepositoryImpl
import com.kshrd.features.auth.domain.repository.AuthRepository
import com.kshrd.features.business.data.repository.BusinessRepositoryImpl
import com.kshrd.features.business.domain.repository.BusinessRepository
import com.kshrd.features.monitor.data.repository.MonitorRepositoryImpl
import com.kshrd.features.monitor.domain.repository.MonitorRepository
import com.kshrd.features.note.data.repository.NoteRepositoryImpl
import com.kshrd.features.note.domain.repository.NoteRepository
import com.kshrd.features.notifications.data.repository.NotificationsRepositoryImpl
import com.kshrd.features.notifications.domain.repository.NotificationsRepository
import com.kshrd.features.products.data.repository.ProductRepositoryImpl
import com.kshrd.features.products.domain.repository.ProductRepository
import com.kshrd.features.website.data.repository.WebsiteRepositoryImpl
import com.kshrd.features.website.domain.repository.WebsiteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Interface-to-implementation bindings for repositories.
 * Use cases are not listed here: a class with `@Inject constructor` is injectable as is.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindWebsiteRepository(impl: WebsiteRepositoryImpl): WebsiteRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindBusinessRepository(impl: BusinessRepositoryImpl): BusinessRepository

    @Binds
    @Singleton
    abstract fun bindMonitorRepository(impl: MonitorRepositoryImpl): MonitorRepository

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository

    /** Lets `:core` (the bell icon) read the unread count without knowing about this feature. */
    @Binds
    @Singleton
    abstract fun bindUnreadCountSource(impl: NotificationsRepositoryImpl): UnreadCountSource
}
