package de.rauschdo.animals.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import de.rauschdo.animals.database.AnimalDao
import de.rauschdo.animals.database.AnimalRepository
import de.rauschdo.animals.database.CategoryDao
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @ViewModelScoped
    fun provideAnimalRepository(
        dao: AnimalDao,
        categoryDao: CategoryDao,
        coroutineDispatcher: CoroutineDispatcher
    ): AnimalRepository {
        return AnimalRepository(dao, categoryDao, coroutineDispatcher)
    }
}