package com.kshrd.features.di

import com.kshrd.features.remote.api.NotificationApi
import com.kshrd.features.remote.api.PaymentApi
import com.kshrd.features.remote.api.ProductApi
import com.kshrd.features.remote.api.ProjectApi
import com.kshrd.features.remote.api.ProvisioningApi
import com.kshrd.features.remote.api.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** The WebBlock v2 API interfaces (docs/API-MAPPING.md), all on :core's authenticated [Retrofit]. */
@Module
@InstallIn(SingletonComponent::class)
object RemoteApiModule {
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideProjectApi(retrofit: Retrofit): ProjectApi = retrofit.create(ProjectApi::class.java)

    @Provides
    @Singleton
    fun provideProductApi(retrofit: Retrofit): ProductApi = retrofit.create(ProductApi::class.java)

    @Provides
    @Singleton
    fun provideProvisioningApi(retrofit: Retrofit): ProvisioningApi = retrofit.create(ProvisioningApi::class.java)

    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi = retrofit.create(PaymentApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)
}
