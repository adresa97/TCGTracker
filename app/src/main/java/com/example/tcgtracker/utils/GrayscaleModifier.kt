package com.example.tcgtracker.utils

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asComposeColorFilter
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

class GreyScaleModifier : DrawModifier {
    override fun ContentDrawScope.draw() {
        val saturationMatrix = ColorMatrix().apply { setSaturation(0.1f) }
        val saturationFilter = ColorMatrixColorFilter(saturationMatrix)
        val paint = Paint().apply {
            colorFilter = saturationFilter.asComposeColorFilter()
        }
        drawIntoCanvas {
            it.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
            drawContent()
            it.restore()
        }
    }
}

fun Modifier.greyScale(): Modifier = this then GreyScaleModifier()