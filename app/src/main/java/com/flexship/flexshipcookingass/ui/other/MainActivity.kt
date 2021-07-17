package com.flexship.flexshipcookingass.ui.other

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.ActivityMainBinding
import com.flexship.flexshipcookingass.other.Constants
import com.flexship.flexshipcookingass.other.DISH_ID_SAFE_ARG
import com.flexship.flexshipcookingass.other.POS_IN_LIST_SAFE_ARG
import com.flexship.flexshipcookingass.services.CookService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var isNotificationRequired = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        navigateToCookingFragment(intent)

        checkService()
    }

    private fun checkService() {
        if (CookService.isWorking && isNotificationRequired) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.alert_title_is_cooking))
                .setMessage(getString(R.string.alert_message_is_cooking))
                .setPositiveButton(R.string.yes) { _, _ ->
                    val bundle = Bundle().apply {
                        putInt(DISH_ID_SAFE_ARG, CookService.currentDishId)
                        putInt(POS_IN_LIST_SAFE_ARG, CookService.posInList)
                    }
                    navController.navigate(
                        R.id.action_categoryFragment_to_cookingFragment,
                        bundle
                    )
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToCookingFragment(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return (Navigation.findNavController(this, R.id.fragmentContainerView).navigateUp()
                || super.onSupportNavigateUp())
    }

    private fun navigateToCookingFragment(intent: Intent?) {
        intent?.let {
            if (it.action == Constants.ACTION_PENDING_INTENT) {
                val dishId = it.getIntExtra(Constants.KEY_DISH_ID, -1)
                val posInList = it.getIntExtra(Constants.KEY_POSITION_IN_LIST, -1)
                val bundle = Bundle().apply {
                    putInt(DISH_ID_SAFE_ARG, dishId)
                    putInt(POS_IN_LIST_SAFE_ARG, posInList)
                }
                isNotificationRequired = false
                navController.navigate(R.id.action_to_cookingFragment, bundle)
            }
        }
    }

}