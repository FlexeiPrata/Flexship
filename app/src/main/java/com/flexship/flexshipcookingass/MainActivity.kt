package com.flexship.flexshipcookingass

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.flexship.flexshipcookingass.databinding.ActivityMainBinding
import com.flexship.flexshipcookingass.other.Constans
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

}