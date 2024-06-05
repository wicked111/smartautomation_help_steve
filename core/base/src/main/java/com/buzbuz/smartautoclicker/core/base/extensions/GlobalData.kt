package com.buzbuz.smartautoclicker.core.base.extensions

import android.util.Log

class GlobalData private constructor() {
    private val dataMap: MutableMap<String, Pair<Long?, Long?>> = mutableMapOf()

    // Function to set values in the map
    fun setValues(key: String, higherLimitText: Long?, lowerLimitText: Long?) {
        dataMap[key] = Pair(higherLimitText, lowerLimitText)
    }

    // Function to get values from the map
    fun getValues(key: String?): Pair<Long?, Long?>? {
        return dataMap[key]
    }

    fun logAllValues() {
        dataMap.forEach { (key, value) ->
            Log.d("GlobalDataAll", "Key: $key, HigherLimit: ${value.first}, LowerLimit: ${value.second}")
        }
    }

    companion object {
        val instance: GlobalData by lazy { GlobalData() }
    }
}


