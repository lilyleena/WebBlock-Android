package com.kshrd.core.di

import com.kshrd.core.BuildConfig
import com.kshrd.core.api.KotlinxConverterFactory
import com.kshrd.core.auth.AuthInterceptor
import com.kshrd.core.auth.KeystoreTokenStore
import com.kshrd.core.auth.SessionApi
import com.kshrd.core.auth.TokenAuthenticator
import com.kshrd.core.auth.TokenStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

private const val TIMEOUT_SECONDS = 30L

/** The client without the auth interceptor and authenticator; only the session refresh uses it. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlainHttp

/**
 * App-wide network bindings for the WebBlock v2 API (docs/API-MAPPING.md section 1). Feature modules get their
 * Retrofit API interfaces from the [Retrofit] instance provided here, which sends the bearer token, renews it once
 * after a 401 and reads JSON with kotlinx.serialization.
 *
 * Debug builds log only the method, URL, status and time (`BASIC`): bodies carry passwords and tokens.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    @PlainHttp
    fun providePlainOkHttpClient(): OkHttpClient {
        val logging =
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            }
        return OkHttpClient
            .Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @PlainHttp plain: OkHttpClient,
        authInterceptor: AuthInterceptor,
        authenticator: TokenAuthenticator,
    ): OkHttpClient =
        plain
            .newBuilder()
            .apply { interceptors().add(0, authInterceptor) }
            .authenticator(authenticator)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = retrofit(client)

    @Provides
    @Singleton
    @PlainHttp
    fun providePlainRetrofit(
        @PlainHttp client: OkHttpClient,
    ): Retrofit = retrofit(client)

    @Provides
    @Singleton
    fun provideSessionApi(
        @PlainHttp retrofit: Retrofit,
    ): SessionApi = retrofit.create(SessionApi::class.java)

    private fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(KotlinxConverterFactory())
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreBindingsModule {
    @Binds
    @Singleton
    abstract fun bindTokenStore(impl: KeystoreTokenStore): TokenStore
}
