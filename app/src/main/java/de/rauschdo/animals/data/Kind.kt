package de.rauschdo.animals.data

import com.google.gson.annotations.SerializedName

data class Kind(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("imageUrls") val imageUrls: List<String>,
    @SerializedName("weight") val weight: String,
    @SerializedName("size") val size: String,
    @SerializedName("lifespan") val lifespan: String,
    @SerializedName("location") val location: String,
    @SerializedName("trivia") val trivia: List<String>,
    @SerializedName("urls") val urls: List<String>,
    @SerializedName("pet") val isPet: Boolean,
    //First list are polygons, second list inside is LatLon Coordinates
    @SerializedName("coordinates") val coordinates: List<List<List<Double>>>,

    @SerializedName("biggest_thread") val biggest_thread: String?,
    @SerializedName("estimate_count") val estimate_count: Int?,
    @SerializedName("features") val features: List<String>?,
    @SerializedName("habitat") val habitat: String?,
    @SerializedName("lifestyle") val lifestyle: String?,
    @SerializedName("predators") val predators: List<String>?,
    @SerializedName("prey") val prey: Prey?,
    @SerializedName("temper") val temper: List<String>?,
)