package com.flexship.flexshipcookingass.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.flexship.flexshipcookingass.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogFragmentToDelete(
    private val title:Int,
    private val message:Int
): DialogFragment() {

    private var actionToDelete : (()->Unit) ?= null

    fun setAction(action: (()->Unit)){
        actionToDelete = action
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setTitle(title)
            .setPositiveButton(R.string.yes){_ ,_ ->
                actionToDelete?.invoke()
            }
            .setNegativeButton(R.string.no){dial, _ ->
                dial.dismiss()

            }
            .create()
    }
}