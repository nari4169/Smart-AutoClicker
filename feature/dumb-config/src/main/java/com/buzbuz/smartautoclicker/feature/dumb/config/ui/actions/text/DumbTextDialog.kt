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
package com.buzbuz.smartautoclicker.feature.dumb.config.ui.actions.text

import android.graphics.Point
import android.graphics.PointF
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toPoint
import androidx.core.graphics.toPointF

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.buzbuz.smartautoclicker.core.base.GESTURE_DURATION_MAX_VALUE
import com.buzbuz.smartautoclicker.core.dumb.domain.model.DumbAction
import com.buzbuz.smartautoclicker.core.dumb.domain.model.REPEAT_COUNT_MAX_VALUE
import com.buzbuz.smartautoclicker.core.dumb.domain.model.REPEAT_COUNT_MIN_VALUE
import com.buzbuz.smartautoclicker.core.dumb.domain.model.REPEAT_DELAY_MAX_MS
import com.buzbuz.smartautoclicker.core.dumb.domain.model.REPEAT_DELAY_MIN_MS
import com.buzbuz.smartautoclicker.core.ui.bindings.DialogNavigationButton
import com.buzbuz.smartautoclicker.core.ui.bindings.setButtonEnabledState
import com.buzbuz.smartautoclicker.core.ui.bindings.setChecked
import com.buzbuz.smartautoclicker.core.ui.bindings.setError
import com.buzbuz.smartautoclicker.core.ui.bindings.setLabel
import com.buzbuz.smartautoclicker.core.ui.bindings.setNumericValue
import com.buzbuz.smartautoclicker.core.ui.bindings.setOnCheckboxClickedListener
import com.buzbuz.smartautoclicker.core.ui.bindings.setOnTextChangedListener
import com.buzbuz.smartautoclicker.core.ui.bindings.setText
import com.buzbuz.smartautoclicker.core.ui.bindings.setup
import com.buzbuz.smartautoclicker.core.ui.overlays.dialog.OverlayDialog
import com.buzbuz.smartautoclicker.core.ui.overlays.menu.PositionSelectorMenu
import com.buzbuz.smartautoclicker.core.ui.overlays.viewModels
import com.buzbuz.smartautoclicker.core.ui.utils.MinMaxInputFilter
import com.buzbuz.smartautoclicker.core.ui.views.actionbrief.ClickDescription
import com.buzbuz.smartautoclicker.core.ui.views.actionbrief.TextDescription
import com.buzbuz.smartautoclicker.feature.dumb.config.R
import com.buzbuz.smartautoclicker.feature.dumb.config.databinding.DialogConfigDumbActionClickBinding
import com.buzbuz.smartautoclicker.feature.dumb.config.databinding.DialogConfigDumbActionTextBinding
import com.buzbuz.smartautoclicker.feature.dumb.config.di.DumbConfigViewModelsEntryPoint

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch

class DumbTextDialog(
    private val dumbText: DumbAction.DumbText,
    private val onConfirmClicked: (DumbAction.DumbText) -> Unit,
    private val onDeleteClicked: (DumbAction.DumbText) -> Unit,
    private val onDismissClicked: () -> Unit,
) : OverlayDialog(R.style.DumbScenarioConfigTheme) {

    /** The view model for this dialog. */
    private val viewModel: DumbTextViewModel by viewModels(
        entryPoint = DumbConfigViewModelsEntryPoint::class.java,
        creator = { dumbTextViewModel() },
    )
    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigDumbActionTextBinding

    override fun onCreateView(): ViewGroup {
        viewModel.setEditedDumbText(dumbText)

        viewBinding = DialogConfigDumbActionTextBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.item_title_dumb_text)

                buttonDismiss.setOnClickListener { onDismissButtonClicked()}
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setOnClickListener { onSaveButtonClicked() }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setOnClickListener { onDeleteButtonClicked() }
                }
            }

            editNameLayout.apply {
                setLabel(R.string.input_field_label_name)
                setOnTextChangedListener { viewModel.setName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }
            hideSoftInputOnFocusLoss(editNameLayout.textField)

            editTextLayout.apply {
                setLabel(R.string.input_field_label_text)
                setOnTextChangedListener { viewModel.setText(it.toString())}
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(60)
                )
            }
            hideSoftInputOnFocusLoss(editTextLayout.textField)

            editPressDurationLayout.apply {
                textField.filters = arrayOf(MinMaxInputFilter(1, GESTURE_DURATION_MAX_VALUE.toInt()))
                setLabel(R.string.input_field_label_click_press_duration)
                setOnTextChangedListener {
                    viewModel.setPressDurationMs(if (it.isNotEmpty()) it.toString().toLong() else 0)
                }
            }
            hideSoftInputOnFocusLoss(editPressDurationLayout.textField)

            editRepeatLayout.apply {
                textField.filters = arrayOf(MinMaxInputFilter(
                    REPEAT_COUNT_MIN_VALUE,
                    REPEAT_COUNT_MAX_VALUE
                ))
                setup(R.string.input_field_label_repeat_count, R.drawable.ic_infinite, disableInputWithCheckbox = true)
                setOnTextChangedListener {
                    viewModel.setRepeatCount(if (it.isNotEmpty()) it.toString().toInt() else 0)
                }
                setOnCheckboxClickedListener(viewModel::toggleInfiniteRepeat)
            }
            hideSoftInputOnFocusLoss(editRepeatLayout.textField)

            editRepeatDelay.apply {
                textField.filters = arrayOf(MinMaxInputFilter(
                    REPEAT_DELAY_MIN_MS.toInt(),
                    REPEAT_DELAY_MAX_MS.toInt(),
                ))
                setLabel(R.string.input_field_label_repeat_delay)
                setOnTextChangedListener {
                    viewModel.setRepeatDelay(if (it.isNotEmpty()) it.toString().toLong() else 0)
                }
            }
            hideSoftInputOnFocusLoss(editRepeatDelay.textField)

            cardClickPosition.setOnClickListener { onPositionCardClicked() }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.isValidDumbText.collect(::updateSaveButton) }
                launch { viewModel.name.collect(viewBinding.editNameLayout::setText) }
                launch { viewModel.text.collect(viewBinding.editTextLayout::setText) }
                launch { viewModel.nameError.collect(viewBinding.editNameLayout::setError)}
                launch { viewModel.textError.collect(viewBinding.editTextLayout::setError)}
                launch { viewModel.pressDuration.collect(::updateDumbClickPressDuration) }
                launch { viewModel.pressDurationError.collect(viewBinding.editPressDurationLayout::setError)}
                launch { viewModel.repeatCount.collect(viewBinding.editRepeatLayout::setNumericValue) }
                launch { viewModel.repeatCountError.collect(viewBinding.editRepeatLayout::setError) }
                launch { viewModel.repeatInfiniteState.collect(viewBinding.editRepeatLayout::setChecked) }
                launch { viewModel.repeatDelay.collect(::updateDumbClickRepeatDelay) }
                launch { viewModel.repeatDelayError.collect(viewBinding.editRepeatDelay::setError)}
                launch { viewModel.clickPositionText.collect(viewBinding.clickSelectorSubtext::setText) }
            }
        }
    }

    private fun onSaveButtonClicked() {
        viewModel.getEditedDumbText()?.let { editedDumbText ->
            debounceUserInteraction {
                viewModel.saveLastConfig(context)
                onConfirmClicked(editedDumbText)
                back()
            }
        }
    }

    private fun onDeleteButtonClicked() {
        viewModel.getEditedDumbText()?.let { editedAction ->
            debounceUserInteraction {
                onDeleteClicked(editedAction)
                back()
            }
        }
    }

    private fun onDismissButtonClicked() {
        debounceUserInteraction {
            onDismissClicked()
            back()
        }
    }

    private fun onPositionCardClicked() {
        viewModel.getEditedDumbText()?.let { dumbClick ->
            overlayManager.navigateTo(
                context = context,
                newOverlay = PositionSelectorMenu(
                    actionDescription = TextDescription(
                        position = dumbClick.position.toEditionPosition(),
                        pressDurationMs = dumbClick.pressDurationMs,
                    ),
                    onConfirm = { description ->
                        viewModel.setPosition((description as? ClickDescription)?.position?.toPoint())
                    }
                ),
                hideCurrent = true,
            )
        }
    }

    private fun updateDumbClickPressDuration(duration: String) {
        viewBinding.editPressDurationLayout.setText(duration, InputType.TYPE_CLASS_NUMBER)
    }

    private fun updateDumbClickRepeatDelay(delay: String) {
        viewBinding.editRepeatDelay.setText(delay, InputType.TYPE_CLASS_NUMBER)
    }

    private fun updateSaveButton(isValidCondition: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, isValidCondition)
    }

    private fun Point.toEditionPosition(): PointF? =
        if (x == 0 && y == 0) null
        else toPointF()

}