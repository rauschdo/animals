package de.rauschdo.animals.data

import com.google.gson.annotations.SerializedName

data class AnimalDataItem(
    @SerializedName("category") val categoryName: String,
    @SerializedName("orderIndex") val orderIndex: Int,
    @SerializedName("assetId") val assetId: String,
    @SerializedName("kinds") val categoryKinds: List<Kind>
)