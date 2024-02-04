package de.rauschdo.animals.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rauschdo.animals.database.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        application: Application,
        dbTypeConverter: DbTypeConverter,
        coordinatesConverter: CoordinatesConverter
    ): AnimalDatabase {
        return Room
            .databaseBuilder(application, AnimalDatabase::class.java, "Animals.db")
            .fallbackToDestructiveMigration()
            .addTypeConverter(dbTypeConverter)
            .addTypeConverter(coordinatesConverter)
            // only temporarily use this or never
//            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideAnimalDao(appDatabase: AnimalDatabase): AnimalDao {
        return appDatabase.animalDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(appDatabase: AnimalDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideDbTypeConverter(): DbTypeConverter {
        return DbTypeConverter()
    }
}