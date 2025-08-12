package com.boogie_knight.tcgtracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.smarttoolfactory.extendedcolors.util.ColorUtil.colorToHSV
import com.smarttoolfactory.extendedcolors.util.HSVUtil.hsvToColorInt

val IconGreen = Color(0xFF58BA88)

val PocketWhite = Color(0xFFEEF6FB)
val PocketBlack = Color(0xFF23272E)

val DarkGreenContainer = getSimilarColor(IconGreen, 0.2f, 0.3f)
val DarkGreenAccent = getSimilarColor(IconGreen, 0.1f, 0.4f)

val LightGreenContainer = getSimilarColor(IconGreen, 0.2f, 0.8f)
val LightGreenAccent = getSimilarColor(IconGreen, 0.1f, 0.9f)

/*
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
 */

fun getSimilarColor(
    color: Color,
    saturation: Float?,
    value: Float?
): Color {
    val hsv = colorToHSV(color)
    if (saturation != null) hsv[1] = saturation
    if (value != null) hsv[2] = value
    return Color(hsvToColorInt(hsv))
}

fun getSimilarColor(
    color: Color,
    minSaturation: Float?,
    maxSaturation: Float?,
    value: Float?
): Color {
    val hsv = colorToHSV(color)
    if (minSaturation != null && hsv[1] < minSaturation) hsv[1] = minSaturation
    else if (maxSaturation != null && hsv[1] > maxSaturation) hsv[1] = maxSaturation
    if (value != null) hsv[2] = value
    return Color(hsvToColorInt(hsv))
}