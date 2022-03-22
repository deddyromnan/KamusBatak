package com.romnan.kamusbatak.feature_entries_finder.di

import com.romnan.kamusbatak.core.data.local.CoreDatabase
import com.romnan.kamusbatak.core.domain.repository.PreferencesRepository
import com.romnan.kamusbatak.feature_entries_finder.data.remote.EntriesFinderApi
import com.romnan.kamusbatak.feature_entries_finder.data.repository.EntriesFinderRepositoryImpl
import com.romnan.kamusbatak.feature_entries_finder.data.repository.OfflineSupportRepositoryImpl
import com.romnan.kamusbatak.feature_entries_finder.domain.repository.EntriesFinderRepository
import com.romnan.kamusbatak.feature_entries_finder.domain.repository.OfflineSupportRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EntriesFinderModule {
    @Provides
    @Singleton
    fun provideRepository(
        coreDb: CoreDatabase,
        api: EntriesFinderApi,
        offlineSupportRepository: OfflineSupportRepository
    ): EntriesFinderRepository {
        return EntriesFinderRepositoryImpl(api, coreDb.dao, offlineSupportRepository)
    }

    @Provides
    @Singleton
    fun provideApi(coreRetrofit: Retrofit): EntriesFinderApi {
        return coreRetrofit.create(EntriesFinderApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOfflineSupportRepository(
        api: EntriesFinderApi,
        coreDb: CoreDatabase,
        prefRepository: PreferencesRepository
    ): OfflineSupportRepository {
        return OfflineSupportRepositoryImpl(
            api = api,
            coreDao = coreDb.dao,
            prefRepository = prefRepository
        )
    }
}