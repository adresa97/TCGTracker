package com.example.tcgtracker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcgtracker.models.Set
import com.example.tcgtracker.ui.theme.PocketBlack
import com.example.tcgtracker.ui.theme.PocketWhite

@Composable
fun SetScreen(
    series: Map<String, List<Set>>,
    colors: Map<String, Color>,
    isListView: Boolean,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    series.forEach { element ->
        val expansionsMap = getExpansionsMap(series, element.key)
        SeriesGroup(element.key, expansionsMap, colors, isListView, onSetTap)
    }
}

fun getExpansionsMap(
    map: Map<String, List<Set>>,
    series: String
): Map<String, List<Set>> {
    return map.getOrDefault(series, listOf()).groupBy({set -> set.expansion})
}

@Composable
fun SeriesGroup(
    series: String,
    expansions: Map<String, List<Set>>,
    colors: Map<String, Color>,
    isListView: Boolean,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
            val spacing = if (isListView) 5.dp else 20.dp
            val verSpace = if (isListView) 20.dp else 40.dp
            val horSpace = if (isListView) 20.dp else 0.dp
            Column(
                Modifier.padding(horizontal = horSpace, verSpace),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                expansions.forEach { expansion ->
                    if (isListView) {
                        CollectionList(
                            expansion.value,
                            colors,
                            onSetTap,
                            Modifier.padding(horizontal = 20.dp)
                        )
                    } else {
                        CollectionRow(
                            expansion.value,
                            onSetTap,
                            Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionList(
    sets: List<Set>,
    colors: Map<String, Color>,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    sets.forEach { set ->
        val color: Color = colors[set.set] ?: MaterialTheme.colorScheme.primaryContainer
        CollectionCell(set, color, onSetTap)
    }
}

@Composable
fun CollectionCell(
    set: Set,
    color: Color,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fontColor = PocketBlack
    Box(
        modifier = modifier.background(color, RoundedCornerShape(10))
            .height(50.dp)
            .clickable{ onSetTap(set.set) }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                contentAlignment = Alignment.Center
            ) {
                // Top left
                Text(
                    modifier = Modifier.align(Alignment.TopStart).offset(y = (-5).dp),
                    text = set.name,
                    textAlign = TextAlign.Left,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = fontColor
                )
                // Top right
                Text(
                    modifier = Modifier.align(Alignment.TopEnd).offset(y = (-5).dp),
                    text = "${set.numbers.all.ownedCards} / ${set.numbers.all.totalCards}",
                    textAlign = TextAlign.Right,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = fontColor
                )
                // Bottom left
                Text(
                    modifier = Modifier.align(Alignment.BottomStart).offset(y = 6.dp),
                    text = set.set,
                    textAlign = TextAlign.Left,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                    color = fontColor
                )
                // Bottom right
                Text(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(y = 6.dp),
                    text = "${set.numbers.all.ownedCards / set.numbers.all.totalCards * 100} %",
                    textAlign = TextAlign.Right,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                    color = fontColor
                )
            }
        }
    }
}

@Composable
fun CollectionRow(
    sets: List<Set>,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var i = 0
        sets.forEach { set ->
            SetButton(
                collection = set.set,
                resourceID = set.cover.id,
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
fun SetButton(
    collection: String,
    resourceID: Int,
    modifier: Modifier = Modifier
) {
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