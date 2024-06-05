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

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.buzbuz.smartautoclicker.core.base.extensions.GlobalData
import com.buzbuz.smartautoclicker.core.domain.model.action.Action

import com.buzbuz.smartautoclicker.core.ui.bindings.setButtonEnabledState
import com.buzbuz.smartautoclicker.core.ui.bindings.setError
import com.buzbuz.smartautoclicker.core.ui.bindings.setLabel
import com.buzbuz.smartautoclicker.core.ui.bindings.setOnTextChangedListener
import com.buzbuz.smartautoclicker.core.ui.bindings.setText
import com.buzbuz.smartautoclicker.core.ui.overlays.dialog.OverlayDialog
import com.buzbuz.smartautoclicker.core.ui.overlays.viewModels
import com.buzbuz.smartautoclicker.core.ui.utils.MinMaxInputFilter
import com.buzbuz.smartautoclicker.feature.scenario.config.R
import com.buzbuz.smartautoclicker.feature.scenario.config.databinding.DialogConfigActionPauseBinding

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch

class PauseDialog(
    private val onConfirmClicked: () -> Unit,
    private val onDeleteClicked: () -> Unit,
    private val onDismissClicked: () -> Unit,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    /** The view model for this dialog. */
    private val viewModel: PauseViewModel by viewModels()

    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigActionPauseBinding


    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigActionPauseBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_overlay_title_pause)

                buttonDismiss.setOnClickListener {
                    debounceUserInteraction {
                        onDismissClicked()
                        back()
                    }
                }
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

            editPauseDurationLayout.apply {
                textField.filters = arrayOf(MinMaxInputFilter(min = 1))
                setLabel(R.string.input_field_label_pause_duration)
                setOnTextChangedListener {
                    viewModel.setPauseDuration(if (it.isNotEmpty()) it.toString().toLong() else null)
                }
            }
            hideSoftInputOnFocusLoss(editPauseDurationLayout.textField)



            if(editHigherLimitEditText.text!!.isEmpty()) {
                editLowerLimitEditText.isEnabled = false
                editHigherLimitEditText.apply {
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        // This method is called before the text is changed.
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            // This method is called when the text is being changed.
                        }

                        override fun afterTextChanged(s: Editable?) {
                            // This method is called after the text has changed.

                            editLowerLimitEditText.isEnabled = true
                           /* val higherLimitText = editHigherLimitEditText.text?.toString()
                            val lowerLimitText = editLowerLimitEditText.text?.toString()
                            Log.d("GlobalDataUpdate1", "Attempting to update higher limit: $higherLimitText, lower limit: $lowerLimitText")

                            GlobalData.instance.setValues(higherLimitText?.toLongOrNull(),lowerLimitText?.toLongOrNull())


                          //  GlobalData.instance.higherLimitText = higherLimitText?.toLongOrNull()
                           // GlobalData.instance.lowerLimitText = lowerLimitText?.toLongOrNull()

                          //  Log.d("GlobalDataValues1", "GlobalData higher limit: ${GlobalData.instance.higherLimitText}, lower limit: ${GlobalData.instance.lowerLimitText}")

                            val randomTime = startGeneratingRandomTime(higherLimitText, lowerLimitText)
                            viewModel.setPauseDuration(randomTime)
                            viewModel.saveLowerLimit(lowerLimitText!!.toLongOrNull()?: 0L)
                            viewModel.saveHigherLimit(higherLimitText!!.toLongOrNull()?: 0L)*/
                        }
                    })
                }
            }

            // Enable editLowerLimitEditText after filling editHigherLimitEditText
            editLowerLimitEditText.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        // This method is called before the text is changed.
                       // specificKeyEditText.isEnabled = false

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // This method is called when the text is being changed.
                    }

                    override fun afterTextChanged(s: Editable?) {
                        try {

                            specificKeyEditText.isEnabled = true
                          /*  val higherLimitText = editHigherLimitEditText.text?.toString()
                            val lowerLimitText = editLowerLimitEditText.text?.toString()

                            GlobalData.instance.setValues(pauseActionKey,higherLimitText?.toLongOrNull(),lowerLimitText?.toLongOrNull())


                            //GlobalData.instance.higherLimitText = higherLimitText?.toLongOrNull()
                            //GlobalData.instance.lowerLimitText = lowerLimitText?.toLongOrNull()
                            //Log.d("GlobalDataValues2", "GlobalData higher limit: ${GlobalData.instance.higherLimitText}, lower limit: ${GlobalData.instance.lowerLimitText}")

                            val randomTime = startGeneratingRandomTime(higherLimitText, lowerLimitText)
                            viewModel.setPauseDuration(randomTime)
                            viewModel.saveLowerLimit(lowerLimitText!!.toLongOrNull()?: 0L)
                            viewModel.saveHigherLimit(higherLimitText!!.toLongOrNull()?: 0L)*/
                        } catch (e: NumberFormatException) {
                            Log.e("GlobalDataError", "Failed to convert text to Long: ${e.message}")
                        }
                    }
                })
            }



            specificKeyEditText.apply {
                doAfterTextChanged {
                    val higherLimitText = editHigherLimitEditText.text?.toString()!!
                    val lowerLimitText = editLowerLimitEditText.text?.toString()!!
                    val key = specificKeyEditText.text.toString()
                    GlobalData.instance.setValues(key,higherLimitText.toLongOrNull(), lowerLimitText.toLongOrNull())
                    val randomTime = startGeneratingRandomTime(higherLimitText, lowerLimitText)   // I want to eliminate this in future as it set the value in upper column
                    viewModel.setPauseDuration(randomTime)
                   // viewModel.saveLowerLimit(lowerLimitText!!.toLongOrNull()?: 0L)
                  //  viewModel.saveHigherLimit(higherLimitText!!.toLongOrNull()?: 0L)
                    GlobalData.instance.logAllValues()


                }
            }

           /* val higherLimit = viewModel.loadHigherLimit()
            val lowerLimit = viewModel.loadLowerLimit()

            // Set loaded values to EditTexts
            if (higherLimit == 0L || lowerLimit == 0L) {
                editHigherLimitEditText.setText("")
                editLowerLimitEditText.setText("")
            } else {
                // Set loaded values to EditTexts only if they are not default
                editHigherLimitEditText.setText(higherLimit.toString())
                editLowerLimitEditText.setText(lowerLimit.toString())
            }*/


        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingAction.collect(::onActionEditingStateChanged) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.name.collect(::updateClickName) }
                launch { viewModel.nameError.collect(viewBinding.editNameLayout::setError)}
                launch { viewModel.pauseDuration.collect(::updatePauseDuration) }
                launch { viewModel.pauseDurationError.collect(viewBinding.editPauseDurationLayout::setError)}
                launch { viewModel.isValidAction.collect(::updateSaveButton) }
            }
        }
    }

    private fun onSaveButtonClicked() {
        debounceUserInteraction {
            viewModel.saveLastConfig()
            onConfirmClicked()
            back()
        }
    }

    private fun onDeleteButtonClicked() {
        debounceUserInteraction {
            onDeleteClicked()
            back()
        }
    }

    private fun updateClickName(newName: String?) {
        viewBinding.editNameLayout.setText(newName)
    }

    private fun updatePauseDuration(newDuration: String?) {
        viewBinding.editPauseDurationLayout.setText(newDuration, InputType.TYPE_CLASS_NUMBER)
    }

    private fun updateSaveButton(isValidCondition: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(com.buzbuz.smartautoclicker.core.ui.bindings.DialogNavigationButton.SAVE, isValidCondition)
    }

    private fun startGeneratingRandomTime(higherLimitText: String?, lowerLimitText: String?): Long? {
        val higherLimit = higherLimitText?.toLongOrNull() ?: 0L
        val lowerLimit = lowerLimitText?.toLongOrNull() ?: 0L
        val randomTime = viewModel.generateRandomTime(higherLimit, lowerLimit)
        return randomTime
    }

    private fun onActionEditingStateChanged(isEditingAction: Boolean) {
        if (!isEditingAction) {
            Log.e(TAG, "Closing PauseDialog because there is no action edited")
            finish()
        }
    }
}



private const val TAG = "PauseDialog"
