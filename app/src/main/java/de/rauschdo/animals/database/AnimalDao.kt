package de.rauschdo.animals.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * The Room Magic is in this file, where you map a method call to an SQL query.
 *
 * When you are using complex data types, such as Date, you have to also supply type converters.
 * To keep this example basic, no types that require type converters are used.
 * See the documentation at
 * https://developer.android.com/topic/libraries/architecture/room.html#type-converters
 */
@Dao
interface AnimalDao {

    @Query("SELECT * FROM animal_table")
    fun getAll(): List<Animal>

    @Query("SELECT * FROM category_table ORDER BY orderIndex")
    fun getAllCategorized(): Flow<List<CategoryWithAnimals>>

    @Query("SELECT * FROM animal_table WHERE name LIKE :name")
    fun get(name: String): Animal?

    @Query("SELECT * FROM animal_table WHERE favourite = :favourite")
    fun getFavoritesFlow(favourite: Boolean = true): Flow<List<Animal>>

    //SQLite dont have boolean Datatype directly so workaround with default param
    @Query("SELECT * FROM animal_table WHERE favourite = :favourite")
    fun getFavorites(favourite: Boolean = true): List<Animal>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(animal: Animal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<Animal>)

    @Query("DELETE FROM animal_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("DELETE FROM animal_table")
    suspend fun deleteAll()

    @Transaction
    @Query("UPDATE animal_table SET favourite = :favourite WHERE id = :id")
    suspend fun setFavourite(id: Long, favourite: Boolean)
}