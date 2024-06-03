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
package com.buzbuz.smartautoclicker.feature.scenario.config.ui.action.pause

import android.app.Application
import android.content.SharedPreferences
import android.util.Log

import androidx.lifecycle.AndroidViewModel
import com.buzbuz.smartautoclicker.core.base.extensions.GlobalData

import com.buzbuz.smartautoclicker.core.domain.model.action.Action
import com.buzbuz.smartautoclicker.feature.scenario.config.domain.EditionRepository
import com.buzbuz.smartautoclicker.feature.scenario.config.utils.getEventConfigPreferences
import com.buzbuz.smartautoclicker.feature.scenario.config.utils.putPauseDurationConfig
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlin.random.Random

@OptIn(FlowPreview::class)
class PauseViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val KEY_HIGHER_LIMIT = "higher_limit"
        private const val KEY_LOWER_LIMIT = "lower_limit"
    }


    /** Repository providing access to the edited items. */
    private val editionRepository = EditionRepository.getInstance(application)
    /** The action being configured by the user. */
    private val configuredPause = editionRepository.editionState.editedActionState
        .mapNotNull { action -> action.value }
        .filterIsInstance<Action.Pause>()
    /** Event configuration shared preferences. */
    private val sharedPreferences: SharedPreferences = application.getEventConfigPreferences()

    /** Tells if the user is currently editing an action. If that's not the case, dialog should be closed. */
    val isEditingAction: Flow<Boolean> = editionRepository.isEditingAction
        .distinctUntilChanged()
        .debounce(1000)

    private var randomTimeJob: Job? = null

    /** The name of the pause. */
    val name: Flow<String?> = configuredPause
        .map { it.name }
        .take(1)
    /** Tells if the action name is valid or not. */
    val nameError: Flow<Boolean> = configuredPause.map { it.name?.isEmpty() ?: true }

    /** The duration of the pause in milliseconds. */
    val pauseDuration: Flow<String?> = configuredPause
        .map { it.pauseDuration?.toString() }
        .take(1)
    /** Tells if the pause duration value is valid or not. */
    val pauseDurationError: Flow<Boolean> = configuredPause.map { (it.pauseDuration ?: -1) <= 0 }

    /** Tells if the configured pause is valid and can be saved. */
    val isValidAction: Flow<Boolean> = editionRepository.editionState.editedActionState
        .map { it.canBeSaved }

    /**
     * Set the name of the pause.
     * @param name the new name.
     */
    fun setName(name: String) {
        editionRepository.editionState.getEditedAction<Action.Pause>()?.let { pause ->
            editionRepository.updateEditedAction(pause.copy(name = "" + name))
        }
    }

    /**
     * Set the duration of the pause.
     * @param durationMs the new duration in milliseconds.
     */
    fun setPauseDuration(durationMs: Long?) {
        editionRepository.editionState.getEditedAction<Action.Pause>()?.let { pause ->
            editionRepository.updateEditedAction(pause.copy(pauseDuration = durationMs))
        }
    }

    fun saveLastConfig() {
        editionRepository.editionState.getEditedAction<Action.Pause>()?.let { pause ->
            sharedPreferences.edit().putPauseDurationConfig(pause.pauseDuration ?: 0).apply()
        }
    }

    fun generateRandomTime(higherLimit: Long, lowerLimit: Long): Long? {
        if (higherLimit < lowerLimit) {
            return null // Return null indicating an error
        }
        val randomTime = Random.nextLong(lowerLimit, higherLimit + 1)
        Log.d("Random Time", randomTime.toString())
        return randomTime
    }

    fun saveHigherLimit(limit: Long) {
        sharedPreferences.edit().putLong(KEY_HIGHER_LIMIT, limit).apply()
    }

    fun saveLowerLimit(limit: Long) {
        sharedPreferences.edit().putLong(KEY_LOWER_LIMIT, limit).apply()
    }

    fun loadHigherLimit(): Long {
        return sharedPreferences.getLong(KEY_HIGHER_LIMIT, 0L)
    }

    fun loadLowerLimit(): Long {
        return sharedPreferences.getLong(KEY_LOWER_LIMIT, 0L)
    }


}