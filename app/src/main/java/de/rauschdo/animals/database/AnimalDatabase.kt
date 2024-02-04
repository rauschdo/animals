package de.rauschdo.animals.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Category::class, Animal::class], version = 1)
@TypeConverters(value = [DbTypeConverter::class, CoordinatesConverter::class])
abstract class AnimalDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun animalDao(): AnimalDao
}
