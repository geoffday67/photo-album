package uk.co.sullenart.photoalbum.main

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

class MainViewmodel(
    private val navController: NavController,
): ViewModel() {
    fun onSettingsClicked() {
        navController.navigate("settings")
    }
}