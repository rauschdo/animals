package de.rauschdo.animals.database

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithAnimals(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "animalCategoryId"
    )
    val animals: List<Animal>
)