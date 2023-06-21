package com.spyneai.dashboard.di

import android.content.Context
import androidx.room.Room
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.room.SpyneAppDatabase
import com.spyneai.dashboard.network.LiveDataCallAdapterFactoryForRetrofit
import com.spyneai.dashboard.repository.model.CategoryDataAppDao
import com.spyneai.needs.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule  {


    @Provides
    fun provideCategoryDataDao(SpyneAppDatabase: SpyneAppDatabase): CategoryDataAppDao {
        return SpyneAppDatabase.categoryDataDao()
    }

    @Provides
    @Singleton
    fun provideSpyneAppDatabase(@ApplicationContext context : Context) : SpyneAppDatabase{
            return Room.databaseBuilder(
                context,
                SpyneAppDatabase::class.java,
                "spyne-db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }

    @Singleton
    @Provides
    fun provideNewsService(): ClipperApi {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactoryForRetrofit())
            .build()
            .create(ClipperApi::class.java)
    }

    }

