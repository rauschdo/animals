package de.rauschdo.animals.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

@ProvidedTypeConverter
class DbTypeConverter @Inject constructor() {

    @TypeConverter
    fun fromStringToList(value: String): List<String>? {
        val listType = object : TypeToken<List<String>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromListToString(list: List<String>?): String {
        return Gson().toJson(list)
    }
}
