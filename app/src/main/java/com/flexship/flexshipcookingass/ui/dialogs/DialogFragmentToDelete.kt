package com.flexship.flexshipcookingass.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.flexship.flexshipcookingass.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogFragmentToDelete: DialogFragment() {

    private var actionToDelete:(()->Unit)?=null

    fun setAction(action: (()->Unit)){
        actionToDelete=action
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setMessage("")
            .setTitle("")
            .setPositiveButton(""){_,_->
                actionToDelete?.invoke()
            }
            .setNegativeButton(""){dial,_->
                dial.dismiss()

            }
            .create()
    }
}