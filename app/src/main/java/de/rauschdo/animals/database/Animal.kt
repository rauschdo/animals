package de.rauschdo.animals.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Entity(tableName = "animal_table", primaryKeys = ["id", "name"])
@Parcelize
data class Animal(

    var animalCategoryId: Long,

    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "iconId")
    var iconId: String?,

    @ColumnInfo(name = "pet")
    var pet: Boolean = false,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "favourite")
    var favourite: Boolean,

    @ColumnInfo(name = "urls")
    var urls: List<String>,

    @ColumnInfo(name = "imageUrls")
    var imageUrls: List<String>,

    @ColumnInfo(name = "location")
    var locationName: String,

    @ColumnInfo(name = "estimate_count")
    var estimatePopulation: Int,

    @ColumnInfo(name = "habitat")
    var habitat: String,

    @ColumnInfo(name = "features")
    var features: List<String>,

    @ColumnInfo(name = "coordinates")
    var coordinates: List<List<List<Double>>>,

    @ColumnInfo(name = "lifespan")
    var lifespan: String,

    @ColumnInfo(name = "size")
    var size: String,

    @ColumnInfo(name = "weight")
    var weight: String,

    @ColumnInfo(name = "temper")
    var temper: List<String>,

    @ColumnInfo(name = "prey")
    var prey: List<String>,

    @ColumnInfo(name = "lifestyle")
    var lifestyleDescription: String,

    @ColumnInfo(name = "predators")
    var predators: List<String>,

    @ColumnInfo(name = "biggest_thread")
    var biggestThread: String,

    @ColumnInfo(name = "trivia")
    var triviaPoints: List<String>
) : Parcelable