package com.example.tcgtracker

import android.graphics.Color.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcgtracker.models.Set
import com.example.tcgtracker.ui.theme.PocketBlack
import com.example.tcgtracker.ui.theme.PocketWhite
import com.smarttoolfactory.extendedcolors.util.ColorUtil.colorToHSV
import com.smarttoolfactory.extendedcolors.util.HSVUtil.hsvToColorInt

@Composable
fun BottomSheet(
    title: String,
    uiColor: Color,
    trackerColor: Color,
    peekArea: Dp,
    safeArea: Dp,
    isFiltersSheet: Boolean,
    onIconClick: (Boolean) -> Unit = {},
    onFiltersChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Peek area
        PeekArea(
            height = peekArea,
            title = title,
            uiColor = uiColor,
            trackerColor = trackerColor,
            onIconClick = { state -> onIconClick(state) }
        )

        // Divider
        val hsv = colorToHSV(uiColor)
        hsv[2] = 0.5f
        val dividerColor = Color(hsvToColorInt(hsv))

        HorizontalDivider(
            thickness = 2.dp,
            color = dividerColor
        )

        // Extendable sheet content
        Box(
            modifier = Modifier.fillMaxWidth()
                .absolutePadding(
                    left = 30.dp,
                    right = 30.dp,
                    top = if (safeArea != 0.dp) safeArea else 30.dp,
                    bottom = if (safeArea != 0.dp) safeArea * 2 else 30.dp
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isFiltersSheet) {
                val filtersState = FiltersManager.getFilters()
                FiltersSheet(
                    checkColor = uiColor,
                    backColor = MaterialTheme.colorScheme.surface,
                    filtersState = filtersState,
                    onFiltersChanged = { onFiltersChanged() }
                )
            } else {
                val seriesMap = SetsData.getSeriesMap()
                val activeFilters = FiltersManager.getActiveFilters()
                InfoSheet(
                    sets = seriesMap,
                    rarities = activeFilters
                )
            }
        }
    }
}

@Composable
fun PeekArea(
    height: Dp,
    title: String,
    uiColor: Color,
    trackerColor: Color,
    onIconClick: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(height),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = {
                onIconClick(false)
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.info),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }
        Box(
            modifier = Modifier.fillMaxHeight(0.6f).fillMaxWidth(0.75f)
                .background(trackerColor, RoundedCornerShape(percent = 50))
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(percent = 50),
                    clip = true,
                    ambientColor = Color(0.0f, 0.0f, 0.0f, 0.0f),
                    spotColor = PocketWhite.apply{ alpha(100) }
                )
                .border(2.dp, uiColor.apply{ alpha(50) }, RoundedCornerShape(percent = 50))
                .wrapContentHeight(align = Alignment.CenterVertically)
                .wrapContentWidth(align = Alignment.CenterHorizontally),
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(
                    fontSize = 26.sp,
                    color = PocketWhite,
                    shadow = Shadow(
                        color = PocketBlack,
                        blurRadius = 10.0f
                    )
                )
            )
        }
        IconButton(
            onClick = {
                onIconClick(true)
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.filter_alt),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }
    }
}

@Composable
fun InfoSheet(
    sets: Map<String, List<Set>>,
    rarities: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(0.dp, 300.dp)
            .padding(all = 10.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        sets.forEach{ series ->
            items(count = series.value.size) { index ->
                val set = series.value[index]
                if (!set.set.contains("P-")) {
                    InfoSetElement(
                        set = set,
                        rarities = rarities
                    )
                }
            }
        }
    }
}

@Composable
fun FiltersSheet(
    checkColor: Color,
    backColor: Color,
    filtersState: Map<String, Boolean>,
    onFiltersChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(all = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Diamonds Column
        RaritiesFilterColumn(
            checkColor = checkColor,
            backColor = backColor,
            filtersState = filtersState,
            rarities = Concepts.getDiamondRarities(),
            onCheckChange = {
                onFiltersChanged()
            },
            modifier = Modifier.weight(1f)
        )

        // Star and crown column
        RaritiesFilterColumn(
            checkColor = checkColor,
            backColor = backColor,
            filtersState = filtersState,
            rarities = Concepts.getStarCrownRarities(),
            onCheckChange = {
                onFiltersChanged()
            },
            modifier = Modifier.weight(1f)
        )

        // Shiny column
        RaritiesFilterColumn(
            checkColor = checkColor,
            backColor = backColor,
            filtersState = filtersState,
            rarities = Concepts.getShinyRarities(),
            onCheckChange = {
                onFiltersChanged()
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun InfoSetElement(
    set: Set,
    rarities: List<String>,
    modifier: Modifier = Modifier
) {
    var setColor = set.color
    val hsv = colorToHSV(setColor)
    if (hsv[1] < 0.4f) hsv[1] = 0.4f
    else if (hsv[1] > 0.6f) hsv[1] = 0.6f
    hsv[2] = 0.9f
    setColor = Color(hsvToColorInt(hsv))

    val fontColor = MaterialTheme.colorScheme.surface

    var probableBooster = SetsData.getMostProbableBooster(set.set, rarities)
    if (probableBooster == null) {
        probableBooster = Pair(OriginsData.getOriginByID(set.origins[0])!!, 0.0f)
    }

    Column(
        modifier = modifier.fillMaxWidth()
            .background(setColor, RoundedCornerShape(5.dp))
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .offset(y = 4.dp)
        ) {
            Text(
                text = "${set.name} (${set.set})",
                fontSize = 18.sp,
                color = fontColor
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterEnd)
                    .offset(y = (-4).dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterStart)
                        .absolutePadding(left = 15.dp),
                    text = probableBooster.first.name,
                    fontSize = 16.sp,
                    color = fontColor
                )
                val percentage = "%.3f".format(probableBooster.second)
                Text(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    text = "${percentage}%",
                    fontSize = 16.sp,
                    color = fontColor
                )
            }
        }
    }
}

@Composable
fun RaritiesFilterColumn(
    checkColor: Color,
    backColor: Color,
    filtersState: Map<String, Boolean>,
    rarities: List<String>,
    onCheckChange: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val checkColors = CheckboxColors(
        checkedCheckmarkColor = backColor,
        uncheckedCheckmarkColor = backColor,
        checkedBoxColor = checkColor,
        uncheckedBoxColor = backColor,
        disabledCheckedBoxColor = backColor,
        disabledUncheckedBoxColor = backColor,
        disabledIndeterminateBoxColor = backColor,
        checkedBorderColor = checkColor,
        uncheckedBorderColor = checkColor,
        disabledBorderColor = backColor,
        disabledUncheckedBorderColor = backColor,
        disabledIndeterminateBorderColor = backColor
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        rarities.forEach { rarity ->
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var isFilterActive: Boolean by rememberSaveable {
                    mutableStateOf(filtersState[rarity]!!)
                }
                Checkbox(
                    modifier = Modifier,
                    checked = isFilterActive,
                    colors = checkColors,
                    onCheckedChange = { isActive ->
                        if (isActive) {
                            FiltersManager.addFilter(rarity)
                        } else {
                            FiltersManager.removeFilter(rarity)
                        }
                        onCheckChange()
                        isFilterActive = isActive
                    }
                )
                Text(
                    text = Concepts.getPrettyRarity(rarity),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}