package de.rauschdo.animals.utility

import android.content.res.Resources
import com.google.gson.GsonBuilder
import de.rauschdo.animals.data.AnimalData
import de.rauschdo.animals.data.Kind
import de.rauschdo.animals.data.Prey
import de.rauschdo.animals.data.viewmodel.AnimalViewModel
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.database.Category
import java.io.IOException
import java.io.InputStream

object DataUtil {

    suspend fun readDataAndInsertInDB(resources: Resources, viewModel: AnimalViewModel): Boolean {
        try {
            val animals = loadAnimalData(resources)
            animals?.forEach {
                val result = viewModel.requestCategory(it.categoryName)

                if (result == null) {
                    viewModel.createCategoryWithAnimals(
                        category = Category(
                            name = it.categoryName,
                            orderIndex = it.orderIndex,
                            assetId = it.assetId
                        ),
                        animals = mapToAnimalDbData(
                            null,
                            it.categoryKinds
                        )
                    )
                } else {
                    viewModel.insertAllAnimals(
                        mapToAnimalDbData(
                            result,
                            it.categoryKinds
                        )
                    )
                }
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun loadAnimalData(resources: Resources): AnimalData? {
        val json: String?
        val inputStream: InputStream = resources.assets.open("data/full_data.json")
        json = inputStream.bufferedReader().use { it.readText() }
        val gson = GsonBuilder().create()
        return try {
            gson.fromJson(json, AnimalData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun mapToAnimalDbData(category: Category?, animals: List<Kind>): List<Animal> {
        return animals.mapIndexed { _, animal ->
            Animal(
                id = animal.id,
                pet = animal.isPet,
                animalCategoryId = category?.categoryId ?: 0,
                iconId = category?.assetId,
                name = animal.name,
                urls = animal.urls,
                imageUrls = animal.imageUrls,
                locationName = animal.location,
                coordinates = animal.coordinates,
                lifespan = animal.lifespan,
                size = animal.size,
                weight = animal.weight,
                estimatePopulation = animal.estimate_count ?: -1,
                habitat = animal.habitat ?: "",
                features = animal.features ?: emptyList(),
                temper = animal.temper ?: emptyList(),
                prey = createPreyList(animal.prey),
                lifestyleDescription = animal.lifestyle ?: "",
                predators = animal.predators ?: emptyList(),
                biggestThread = animal.biggest_thread ?: "",
                triviaPoints = animal.trivia,
                favourite = false
            )
        }
    }

    private fun createPreyList(prey: Prey?): List<String> {
        return prey?.primary?.map { it }
            ?.union(prey.secondary?.map { it } ?: emptyList())
            ?.toList() ?: emptyList()
    }
}