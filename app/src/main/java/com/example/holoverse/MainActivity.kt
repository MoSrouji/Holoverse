package com.example.holoverse
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.holoverse.navigation.AppNavHost
import com.example.holoverse.navigation.AppNavigator
import com.example.holoverse.ui.home.HomeScreen
import com.example.holoverse.ui.theme.HoloverseTheme
import javax.inject.Inject

@AndroidEntryPoint

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: AppNavigator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HoloverseTheme(){
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //SpatialBackground()
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        navigator = navigator
                    )
                   // HomeScreen()

                }
            }
        }
    }
}
