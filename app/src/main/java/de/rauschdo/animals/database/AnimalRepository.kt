package de.rauschdo.animals.database

import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AnimalRepository @Inject constructor(
    private val dao: AnimalDao,
    private val categoryDao: CategoryDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    @MainThread
    fun fetchAnimalCategoryList() = flow<List<CategoryWithAnimals>> {
        // Emit Database
        val data = dao.getAllCategorized()
        // Maybe later network as well if finding suiting API
        emitAll(data)
    }.flowOn(ioDispatcher)

    @MainThread
    fun fetchFavouriteList() = flow<List<Animal>> {
        val data = dao.getFavoritesFlow()
        emitAll(data)
    }.flowOn(ioDispatcher)

    @MainThread
    fun fetchCategoryList() = flow<List<Category>> {
        val data = categoryDao.getAll()
        emitAll(data)
    }.flowOn(ioDispatcher)

    suspend fun insert(animal: Animal) {
        return dao.insert(animal)
    }

    suspend fun insertAllAnimals(animals: List<Animal>) {
        return dao.insertAll(animals)
    }

    suspend fun deleteSingleAnimal(id: Long) {
        dao.deleteById(id)
    }

    fun getAnimalFavourties() = dao.getFavorites()

    suspend fun setAnimalFavourite(animalId: Long, favourite: Boolean) =
        dao.setFavourite(animalId, favourite)

    fun getCategory(name: String): Category? = categoryDao.get(name)

    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun insertCategoryWithAnimals(category: Category, animalsOfCategory: List<Animal>) {
        categoryDao.insertCategoryWithAnimals(category, animalsOfCategory)
    }

    suspend fun updateCategoryOrder(id: Long, index: Int) = categoryDao.setOrderIndex(id, index)
}