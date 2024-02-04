package de.rauschdo.animals.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAnimal(animal: Animal)

    @Query("SELECT * FROM category_table WHERE name LIKE :name")
    fun get(name: String): Category?

    @Query("SELECT * FROM category_table ORDER BY orderIndex")
    fun getAll(): Flow<List<Category>>

    @Transaction
    @Query("UPDATE category_table SET orderIndex = :newIndex WHERE categoryId = :id")
    suspend fun setOrderIndex(id: Long, newIndex: Int)

    @Transaction
    suspend fun insertCategoryWithAnimals(category: Category, animalsOfCategory: List<Animal>) {
        val categoryId = insert(category)
        animalsOfCategory.forEach { animal ->
            animal.animalCategoryId = categoryId
            animal.iconId = category.assetId
            insertAnimal(animal)
        }
    }
}