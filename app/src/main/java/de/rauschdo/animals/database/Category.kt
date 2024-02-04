package de.rauschdo.animals.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class Category(

    @PrimaryKey(autoGenerate = true)
    var categoryId: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "orderIndex")
    var orderIndex: Int,

    @ColumnInfo(name = "assetId")
    var assetId: String,
)