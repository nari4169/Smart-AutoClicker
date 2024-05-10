/*
 * Copyright (C) 2024 Kevin Buzeau
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
package com.buzbuz.smartautoclicker.feature.dumb.config.ui.actions.text

import android.content.Context
import android.graphics.Point

import androidx.lifecycle.ViewModel

import com.buzbuz.smartautoclicker.core.dumb.domain.model.DumbAction
import com.buzbuz.smartautoclicker.feature.dumb.config.R
import com.buzbuz.smartautoclicker.feature.dumb.config.data.getDumbConfigPreferences
import com.buzbuz.smartautoclicker.feature.dumb.config.data.putClickPressDurationConfig
import com.buzbuz.smartautoclicker.feature.dumb.config.data.putClickRepeatCountConfig
import com.buzbuz.smartautoclicker.feature.dumb.config.data.putClickRepeatDelayConfig
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import javax.inject.Inject

class DumbTextViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val _editedDumbText: MutableStateFlow<DumbAction.DumbText?> = MutableStateFlow(null)
    private val editedDumbText: Flow<DumbAction.DumbText> = _editedDumbText.filterNotNull()

    /** Tells if the configured dumb click is valid and can be saved. */
    val isValidDumbClick: Flow<Boolean> = _editedDumbText
        .map { it != null && it.isValid() }

    /** The name of the click. */
    val name: Flow<String> = _editedDumbText
        .map { it!!.name }
        .take(1)
    /** Tells if the action name is valid or not. */
    val nameError: Flow<Boolean> = _editedDumbText
        .map { it!!.name.isEmpty() }

    /** The duration between the press and release of the click in milliseconds. */
    val pressDuration: Flow<String> = editedDumbText
        .map { it.pressDurationMs.toString() }
        .take(1)
    /** Tells if the press duration value is valid or not. */
    val pressDurationError: Flow<Boolean> = editedDumbText
        .map { it.pressDurationMs <= 0 }

    /** The number of times to repeat the action. */
    val repeatCount: Flow<String> = editedDumbText
        .map { it.repeatCount.toString() }
        .take(1)
    /** Tells if the repeat count value is valid or not. */
    val repeatCountError: Flow<Boolean> = editedDumbText
        .map { it.repeatCount <= 0 }
    /** Tells if the action should be repeated infinitely. */
    val repeatInfiniteState: Flow<Boolean> = editedDumbText
        .map { it.isRepeatInfinite }
    /** The delay, in ms, between two repeats of the action. */
    val repeatDelay: Flow<String> = editedDumbText
        .map { it.repeatDelayMs.toString() }
        .take(1)
    /** Tells if the delay is valid or not. */
    val repeatDelayError: Flow<Boolean> = editedDumbText
        .map { !it.isRepeatDelayValid() }

    /** Subtext for the position selector. */
    val clickPositionText: Flow<String> = editedDumbText
        .map { dumbClick ->
            context.getString(
                R.string.item_desc_dumb_click_on_position,
                dumbClick.position.x,
                dumbClick.position.y,
            )
        }

    fun setEditedDumbText(click: DumbAction.DumbText) {
        _editedDumbText.value = click.copy()
    }

    fun getEditedDumbText(): DumbAction.DumbText? =
        _editedDumbText.value

    fun setName(newName: String) {
        _editedDumbText.value = _editedDumbText.value?.copy(name = newName)
    }

    fun setText(newText: String) {
        _editedDumbText.value = _editedDumbText.value?.copy(text = newText)
    }

    fun setPressDurationMs(durationMs: Long) {
        _editedDumbText.value = _editedDumbText.value?.copy(pressDurationMs = durationMs)
    }

    fun setRepeatCount(repeatCount: Int) {
        _editedDumbText.value = _editedDumbText.value?.copy(repeatCount = repeatCount)
    }

    fun toggleInfiniteRepeat() {
        val currentValue = _editedDumbText.value?.isRepeatInfinite ?: return
        _editedDumbText.value = _editedDumbText.value?.copy(isRepeatInfinite = !currentValue)
    }

    fun setRepeatDelay(delayMs: Long) {
        _editedDumbText.value = _editedDumbText.value?.copy(repeatDelayMs = delayMs)
    }

    fun setPosition(position: Point?) {
        position ?: return
        _editedDumbText.value = _editedDumbText.value?.copy(position = position)
    }

    fun saveLastConfig(context: Context) {
        _editedDumbText.value?.let { click ->
            context.getDumbConfigPreferences()
                .edit()
                .putClickPressDurationConfig(click.pressDurationMs)
                .putClickRepeatCountConfig(click.repeatCount)
                .putClickRepeatDelayConfig(click.repeatDelayMs)
                .apply()
        }
    }
}