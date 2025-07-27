package com.example.tcgtracker

import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tcgtracker.ui.theme.TCGTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: Make it Asynchronous
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TCGTrackerTheme {
                TCGTrackerApp(applicationContext)
            }
        }
    }
}

enum class Screen(@StringRes val title: Int) {
    SetSelector(title = R.string.app_name),
    CardViewer(title = R.string.card_viewer)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TCGTrackerApp(
    context: Context,
    navController: NavHostController = rememberNavController()
) {
    /*
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.SetSelector.name
    )
     */
    
    val collections = CollectionsData(context, "collections.json")
    val service = TCGDexService("en")

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text("TCGTracker")}
            )
            /*
            TCGTrackerTopBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )

             */
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Bottom bar"
                )
            }
        }
    ) { innerPadding ->
        var selectedSet = "A1"

        NavHost(
            navController = navController,
            startDestination = Screen.SetSelector.name,
            modifier = Modifier.padding(innerPadding)
        )
        {
            composable(Screen.SetSelector.name) {
                SetSelectorList(
                    series = collections.getSeriesMap(),
                    onSetTap = {
                        selectedSet = it
                        navController.navigate(Screen.CardViewer.name)
                    }
                )
            }

            composable(Screen.CardViewer.name) {
                CardsGrid(service, selectedSet)
            }
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TCGTrackerTopBar(
    currentScreen: Screen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = { Text(stringResource(currentScreen.title))},
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        }
    )
}
 */

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun TCGTrackerPreview() {
    val collections = CollectionsData(LocalContext.current, "collections.json")
    val service = TCGDexService("en")

    TCGTrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text("TCGTracker")
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Bottom bar"
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding))
            {
                //SetSelectorList(collections.getSeriesMap())
            }
        }
    }
}