package com.kshrd.features.di

import com.kshrd.core.AppConfig
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
import com.kshrd.features.remote.repository.ApiAuthRepository
import com.kshrd.features.remote.repository.ApiBusinessRepository
import com.kshrd.features.remote.repository.ApiMonitorRepository
import com.kshrd.features.remote.repository.ApiNotificationsRepository
import com.kshrd.features.remote.repository.ApiProductRepository
import com.kshrd.features.remote.repository.ApiWebsiteRepository
import com.kshrd.features.website.data.repository.WebsiteRepositoryImpl
import com.kshrd.features.website.domain.repository.WebsiteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Interface-to-implementation bindings for repositories.
 * Use cases are not listed here: a class with `@Inject constructor` is injectable as is.
 *
 * Each owner-app repository has two implementations: the WebBlock v2 API (`Api*Repository`, the default) and the
 * in-memory sample data (`*RepositoryImpl`, with `use.mock.data=true` in local.properties). Only the chosen one is
 * created.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    companion object {
        private fun <T> pick(
            mock: Provider<out T>,
            api: Provider<out T>,
        ): T = if (AppConfig.useMockData) mock.get() else api.get()

        @Provides
        @Singleton
        fun provideAuthRepository(
            mock: Provider<AuthRepositoryImpl>,
            api: Provider<ApiAuthRepository>,
        ): AuthRepository = pick(mock, api)

        @Provides
        @Singleton
        fun provideWebsiteRepository(
            mock: Provider<WebsiteRepositoryImpl>,
            api: Provider<ApiWebsiteRepository>,
        ): WebsiteRepository = pick(mock, api)

        @Provides
        @Singleton
        fun provideProductRepository(
            mock: Provider<ProductRepositoryImpl>,
            api: Provider<ApiProductRepository>,
        ): ProductRepository = pick(mock, api)

        @Provides
        @Singleton
        fun provideBusinessRepository(
            mock: Provider<BusinessRepositoryImpl>,
            api: Provider<ApiBusinessRepository>,
        ): BusinessRepository = pick(mock, api)

        @Provides
        @Singleton
        fun provideMonitorRepository(
            mock: Provider<MonitorRepositoryImpl>,
            api: Provider<ApiMonitorRepository>,
        ): MonitorRepository = pick(mock, api)

        @Provides
        @Singleton
        fun provideNotificationsRepository(
            mock: Provider<NotificationsRepositoryImpl>,
            api: Provider<ApiNotificationsRepository>,
        ): NotificationsRepository = pick(mock, api)

        /** Lets `:core` (the bell icon) read the unread count without knowing about this feature. */
        @Provides
        @Singleton
        fun provideUnreadCountSource(
            mock: Provider<NotificationsRepositoryImpl>,
            api: Provider<ApiNotificationsRepository>,
        ): UnreadCountSource = pick(mock, api)
    }
}
