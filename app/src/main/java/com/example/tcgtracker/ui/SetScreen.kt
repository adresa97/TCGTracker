package com.example.tcgtracker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcgtracker.models.Set
import kotlin.collections.forEach

@Composable
fun SetScreen(series: Map<String, List<Set>>, onSetTap: (String) -> Unit, modifier: Modifier = Modifier) {
    series.forEach { element ->
        val expansionsMap = getExpansionsMap(series, element.key)
        SeriesGroup(element.key, expansionsMap, onSetTap)
    }
}

fun getExpansionsMap(map: Map<String, List<Set>>, series: String): Map<String, List<Set>> {
    return map.getOrDefault(series, listOf()).groupBy({set -> set.expansion})
}

@Composable
fun SeriesGroup(series: String, expansions: Map<String, List<Set>>, onSetTap: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        stickyHeader {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                    text = series,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        item {
            expansions.forEach { expansion ->
                CollectionRow(expansion.value, onSetTap)
            }
        }
    }
}

@Composable
fun CollectionRow(sets: List<Set>, onSetTap: (String) -> Unit, modifier: Modifier = Modifier)
{
    Row(
        modifier = modifier.fillMaxWidth().padding(all = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var i = 0
        sets.forEach { set ->
            SetButton(
                collection = set.set,
                resourceID = set.cover,
                modifier = Modifier.weight(1f)
                    .clickable { onSetTap(set.set) }
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

@Composable
fun SetButton(collection: String, resourceID: Int, modifier: Modifier = Modifier)
{
    val image = painterResource(resourceID)
    Column(
        modifier = modifier,
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