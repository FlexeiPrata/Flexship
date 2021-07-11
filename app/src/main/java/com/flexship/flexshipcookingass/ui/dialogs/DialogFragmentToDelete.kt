package com.flexship.flexshipcookingass.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.other.MESSAGE
import com.flexship.flexshipcookingass.other.TITLE
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogFragmentToDelete
: DialogFragment() {

    private var actionToDelete : (()->Unit) ?= null


    fun setAction(action: (()->Unit)){
        actionToDelete = action
    }

    companion object {
        fun newInstance(title: String, message: String) : DialogFragmentToDelete {
            val args = Bundle().apply {
                putString(TITLE, title)
                putString(MESSAGE, message)
            }
            return DialogFragmentToDelete().apply {
                arguments = args
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setMessage(arguments?.getString(TITLE) ?: "")
            .setTitle(arguments?.getString(MESSAGE) ?: "")
            .setPositiveButton(R.string.yes){_ ,_ ->
                actionToDelete?.invoke()
            }
            .setNegativeButton(R.string.no){dial, _ ->
                dial.dismiss()

            }
            .create()
    }
}