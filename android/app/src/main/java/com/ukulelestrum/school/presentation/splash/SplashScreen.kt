package com.ukulelestrum.school.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ukulelestrum.school.R
import com.ukulelestrum.school.core.di.ServiceLocator
import com.ukulelestrum.school.core.theme.*
import com.ukulelestrum.school.core.ui.TropicalBackground
import com.ukulelestrum.school.presentation.common.contentRes

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    val finished by model.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished) onFinished() }
    SplashScreen(state)
}

@Composable
fun SplashScreen(state: SplashUiState) {
    val transition = rememberInfiniteTransition(label = "studio")
    val lift by transition.animateFloat(-5f, 5f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "lift")
    val rotation by transition.animateFloat(-2f, 2f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "sway")
    val ripple by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "ripple")
    val status = stringResource(R.string.splash_status)
    TropicalBackground(0) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(28.dp)
            .semantics { stateDescription = status }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.height(32.dp))
            Box(Modifier.size(248.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Leaf.copy(alpha = 0.09f))
                    repeat(2) { index ->
                        val phase = (ripple + index * 0.5f) % 1f
                        drawCircle(Leaf.copy(alpha = (1f - phase) * 0.18f), (48 + phase * 68).dp.toPx(), Offset(size.width / 2, size.height / 2), style = Stroke(2.dp.toPx()))
                    }
                }
                Image(painterResource(R.drawable.logo), null, Modifier.offset(y = lift.dp).size(184.dp).rotate(rotation), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.height(30.dp))
            Text(stringResource(contentRes(state.titleKey)), Modifier.widthIn(max = 290.dp).semantics { heading() }, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(contentRes(state.subtitleKey)), style = MaterialTheme.typography.bodyLarge, color = Mist.copy(alpha = 0.82f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            LinearProgressIndicator(Modifier.width(144.dp).height(4.dp), color = Leaf, trackColor = Mist.copy(alpha = 0.12f))
            Spacer(Modifier.height(16.dp))
            Text(status, style = MaterialTheme.typography.labelSmall, color = Mist.copy(alpha = 0.70f))
            Spacer(Modifier.height(32.dp))
        }
    }
}
