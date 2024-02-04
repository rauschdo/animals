package de.rauschdo.animals.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.skydoves.bindables.BindingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.database.AnimalRepository
import de.rauschdo.animals.database.Category
import de.rauschdo.animals.database.CategoryWithAnimals
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimalViewModel @Inject constructor(private val repository: AnimalRepository) :
    BindingViewModel() {

    private val _itemList = repository.fetchAnimalCategoryList().asLiveData()
    val itemList: LiveData<List<CategoryWithAnimals>>
        get() = _itemList

    private val _favorites = repository.fetchFavouriteList().asLiveData()
    val favourites: LiveData<List<Animal>>
        get() = _favorites

    private val _categories = repository.fetchCategoryList().asLiveData()
    val categories: LiveData<List<Category>>
        get() = _categories

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(animal: Animal) = viewModelScope.launch {
        repository.insert(animal)
    }

    fun insertAllAnimals(animals: List<Animal>) = viewModelScope.launch {
        repository.insertAllAnimals(animals)
    }

    fun deleteAnimal(id: Long) = viewModelScope.launch {
        repository.deleteSingleAnimal(id)
    }

    fun getAnimalFavourties() = repository.getAnimalFavourties()

    suspend fun setAnimalFavourite(animalId: Long, favourite: Boolean) =
        repository.setAnimalFavourite(animalId, favourite)

    fun requestCategory(name: String) = repository.getCategory(name)

    fun createCategoryWithAnimals(category: Category, animals: List<Animal>) =
        viewModelScope.launch {
            repository.insertCategoryWithAnimals(category, animals)
        }

    fun updateCategoryOrderIndex(id: Long, index: Int) = viewModelScope.launch {
        repository.updateCategoryOrder(id, index)
    }
}