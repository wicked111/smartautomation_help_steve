/*
 * Copyright (C) 2023 Kevin Buzeau
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.core.base.extensions

import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import com.buzbuz.smartautoclicker.core.base.GESTURE_DURATION_MAX_VALUE
import com.buzbuz.smartautoclicker.core.base.extensions.GlobalData
import com.buzbuz.smartautoclicker.core.base.identifier.Identifier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.math.sqrt
import kotlin.random.Random

fun Random.nextFloat(from: Float, until: Float): Float =
    (until - from) * nextFloat()

fun Random.getNextPositionIn(area: RectF): PointF =
    PointF(nextFloat(area.left, area.right), nextFloat(area.top, area.bottom))

fun Random.getRandomizedPosition(position: Int): Float = nextInt(
    from = max(position - RANDOMIZATION_POSITION_MAX_OFFSET_PX, 0),
    until = position + RANDOMIZATION_POSITION_MAX_OFFSET_PX + 1,
).toFloat()



fun Random.nextGaussian(): Double {
    // Box-Muller transform
    val u = nextDouble()
    val v = nextDouble()
    return sqrt(-2.0 * ln(u)) * cos(2.0 * PI * v)
}

fun Random.getRandomizedDuration(duration: Long, key: String?): Long {
    val limitPair = GlobalData.instance.getValues(key)

    Log.d("GlobalDataAccess", "Accessing values for key: $key, higher limit: ${limitPair?.first}, lower limit: ${limitPair?.second}")

    return if (limitPair?.first == null || limitPair.second == null) {
        duration
    } else {
        val lowerLimit = max(limitPair.second!!.toLong(), 1)
        val upperLimit = limitPair.first!!.toLong() + 1

        // Generating a more random value using normal distribution
        val mean = (lowerLimit + upperLimit) / 2.0
        val stdDev = (upperLimit - lowerLimit) / 6.0 // 99.7% values within lower and upper limit

        val randomValue = nextGaussian() * stdDev + mean
        randomValue.roundToLong().coerceIn(lowerLimit, upperLimit)
    }
}



fun Random.getRandomizedGestureDuration(duration: Long): Long = nextLong(
    from = max(duration - RANDOMIZATION_DURATION_MAX_OFFSET_MS, 1),
    until = min(duration + RANDOMIZATION_DURATION_MAX_OFFSET_MS + 1, GESTURE_DURATION_MAX_VALUE),
)




/** */
private const val RANDOMIZATION_POSITION_MAX_OFFSET_PX = 5
/** */
private const val RANDOMIZATION_DURATION_MAX_OFFSET_MS = 5
/** For pause randomization*/
private const val RANDOMIZATION_PAUSE_DURATION_MAX_OFFSET_MS = 9000L