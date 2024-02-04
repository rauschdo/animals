package de.rauschdo.animals.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

@ProvidedTypeConverter
class CoordinatesConverter @Inject constructor() {

    @TypeConverter
    fun fromStringToPolyList(value: String): List<List<List<Double>>> {
        val listType = object : TypeToken<List<List<List<Double>>>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromPolyListToString(list: List<List<List<Double>>>): String {
        return Gson().toJson(list)
    }
}
