package de.rauschdo.animals.data

import com.google.gson.annotations.SerializedName

data class Prey(
    @SerializedName("primary") val primary: List<String>,
    @SerializedName("secondary") val secondary: List<String>?
)