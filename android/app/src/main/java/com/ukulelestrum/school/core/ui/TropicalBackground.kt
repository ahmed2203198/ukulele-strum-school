package com.ukulelestrum.school.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ukulelestrum.school.R
import com.ukulelestrum.school.core.theme.*

@Composable
fun TropicalBackground(mood: Int = 1, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        val colors = when (mood) {
            0 -> listOf(Forest.copy(alpha = 0.94f), Forest.copy(alpha = 0.86f), Forest.copy(alpha = 0.98f))
            2 -> listOf(Lagoon.copy(alpha = 0.95f), Forest.copy(alpha = 0.96f), Forest.copy(alpha = 0.91f))
            else -> listOf(Forest.copy(alpha = 0.94f), Lagoon.copy(alpha = 0.88f), Forest.copy(alpha = 0.97f))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        content()
    }
}

@Composable
fun StudioPage(mood: Int = 1, content: @Composable ColumnScope.() -> Unit) {
    TropicalBackground(mood) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        }
    }
}
