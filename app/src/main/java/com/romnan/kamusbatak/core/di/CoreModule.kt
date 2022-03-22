package com.romnan.kamusbatak.core.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.romnan.kamusbatak.core.data.local.CoreDatabase
import com.romnan.kamusbatak.core.data.remote.CoreApiInfo
import com.romnan.kamusbatak.core.data.repository.PreferencesRepositoryImpl
import com.romnan.kamusbatak.core.domain.repository.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoreModule {
    @Provides
    @Singleton
    fun provideCoreDatabase(app: Application): CoreDatabase {
        return Room
            .databaseBuilder(
                app,
                CoreDatabase::class.java,
                CoreDatabase.NAME
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCoreRetrofit(): Retrofit {
        val interceptor = Interceptor { chain ->
            val request = chain
                .request()
                .newBuilder()
                .addHeader(
                    CoreApiInfo.keyParam(),
                    CoreApiInfo.keyValue()
                )
                .build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(CoreApiInfo.baseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(context)
    }
}