package com.flexship.flexshipcookingass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.flexship.flexshipcookingass.databinding.ActivityMainBinding
import com.flexship.flexshipcookingass.other.Constans
import com.flexship.flexshipcookingass.other.LOG_ID
import com.flexship.flexshipcookingass.services.CookService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        navigateToCookingFragment(intent)

        //checkIfServiceAvailable()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToCookingFragment(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return (Navigation.findNavController(this, R.id.fragmentContainerView).navigateUp()
                || super.onSupportNavigateUp())
    }

    private fun navigateToCookingFragment(intent: Intent?){
        intent?.let{
            if(it.action==Constans.ACTION_PENDING_INTENT){
                val dishId=it.getIntExtra(Constans.KEY_DISH_ID,-1)
                val posInList=it.getIntExtra(Constans.KEY_POSITION_IN_LIST,-1)
                val bundle=Bundle().apply {
                    putInt("dishId",dishId)
                    putInt("posInList",posInList)
                }
                navController.navigate(R.id.action_to_cookingFragment,bundle)
            }
        }

    }
    private fun checkIfServiceAvailable(){
        if(CookService.isWorking){
            alertDialogToReturn()
        }
    }
    private fun alertDialogToReturn() = MaterialAlertDialogBuilder(this)
        .setTitle("Не завершенная готовка")
        .setMessage("Вы не завершили предыдущую готовку." +
                "Пока вы не завершите текущую готовку,вы не сможете начать новую." +
                "Желаете вернуться к ней?")
        .setPositiveButton(R.string.yes){
            _,_->
            navigateToCookingFragment(intent)
        }
        .setNegativeButton(R.string.no,null)
        .create()
        .show()

}