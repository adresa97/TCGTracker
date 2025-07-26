package com.example.tcgtracker

import android.graphics.drawable.shapes.Shape
import android.graphics.fonts.FontStyle
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcgtracker.ui.theme.TCGTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val collectionData = CollectionData(application.applicationContext, "collections.json")
        enableEdgeToEdge()
        setContent {
            TCGTrackerTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            stickyHeader {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                                        text = "A",
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            item {
                                collectionData.getExpansionsMap("A").forEach { expansion ->
                                    CollectionRow(expansion.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionButton(collection: String, resourceID: Int, modifier: Modifier = Modifier)
{
    val image = painterResource(resourceID)
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        Text(
            text = collection,
            modifier = Modifier.padding(bottom = 0.dp)
        )
    }
}

@Composable
fun EmptyBox(modifier: Modifier = Modifier) {
    Box(
        modifier
    ) {
        
    }
}

@Composable
fun CollectionRow(sets: List<Set>, modifier: Modifier = Modifier)
{
    Row(
        modifier.fillMaxWidth().padding(all = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var i = 0
        sets.forEach { set ->
            CollectionButton(
                collection = set.set,
                resourceID = set.cover,
                modifier = Modifier.weight(1f)
            )

            i++
        }

        while(i < 3)
        {
            EmptyBox(
                modifier = Modifier.weight(1f)
            )

            i++
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CollectionPreview()
{
    val collectionData = CollectionData(LocalContext.current, "collections.json")
    TCGTrackerTheme {
        collectionData.getExpansionsMap("A").forEach { expansion->
            CollectionRow(expansion.value)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun TCGTrackerPreview() {
    val collectionData = CollectionData(LocalContext.current, "collections.json")
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    stickyHeader {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                                text = "A",
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    item {
                        collectionData.getExpansionsMap("A").forEach { expansion ->
                            CollectionRow(expansion.value)
                        }
                    }
                }
            }
        }
    }
}