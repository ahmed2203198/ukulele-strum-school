package com.ukulelestrum.school.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ukulelestrum.school.R
import com.ukulelestrum.school.core.theme.*
import com.ukulelestrum.school.domain.model.SaveStatus

@Composable
fun PrimaryButton(label: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val shape = RoundedCornerShape(16.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.98f else 1f, spring(dampingRatio = 0.85f, stiffness = 500f), label = "press")
    val colors = if (enabled) listOf(Coral.copy(alpha = 0.78f), Coral.copy(alpha = 0.60f)) else listOf(Lagoon.copy(alpha = 0.5f), Lagoon.copy(alpha = 0.5f))
    Button(
        onClick, modifier.fillMaxWidth().heightIn(min = 60.dp).graphicsLayer { scaleX = scale; scaleY = scale }.shadow(if (enabled) 6.dp else 0.dp, shape)
            .clip(shape).background(Forest).background(Brush.verticalGradient(colors)),
        enabled = enabled, shape = shape, interactionSource = interaction,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Mist, disabledContainerColor = Color.Transparent, disabledContentColor = Mist.copy(alpha = 0.65f)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) { Text(stringResource(label), textAlign = TextAlign.Center) }
}

@Composable
fun SecondaryButton(label: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, image: Int? = null) {
    OutlinedButton(onClick, modifier.heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Mist.copy(alpha = if (enabled) 0.20f else 0.10f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Lagoon.copy(alpha = 0.35f), contentColor = Mist, disabledContentColor = Mist.copy(alpha = 0.60f))) {
        val painter = when (image) {
            R.drawable.icon_home -> painterResource(R.drawable.icon_home)
            R.drawable.icon_pause -> painterResource(R.drawable.icon_pause)
            else -> null
        }
        if (painter != null) {
            Image(painter, null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(8.dp))
        }
        Text(stringResource(label), textAlign = TextAlign.Center)
    }
}

@Composable
fun StudioCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Lagoon.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, Mist.copy(alpha = 0.10f)), shadowElevation = 5.dp) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, onMenu: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = Forest.copy(alpha = 0.62f), border = BorderStroke(1.dp, Mist.copy(alpha = 0.10f))) {
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SecondaryButton(R.string.back, onBack)
                if (onMenu != null) SecondaryButton(R.string.menu, onMenu, image = R.drawable.icon_home)
            }
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
    }
}

@Composable
fun StatusMessage(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, color = Mist.copy(alpha = 0.86f), style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun SaveConfirmation(status: SaveStatus, onRetry: () -> Unit) {
    if (status != SaveStatus.Saved) {
        StatusMessage(stringResource(if (status == SaveStatus.Saving) R.string.saving else R.string.not_saved))
        if (status == SaveStatus.Failed) SecondaryButton(R.string.retry_save, onRetry)
    }
}

@Composable
fun StatCard(value: String, label: Int, modifier: Modifier = Modifier) {
    StudioCard(modifier) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = Mist)
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = Mist.copy(alpha = 0.80f))
    }
}

@Composable
fun TempoStepper(tempo: Int, onChange: (Int) -> Unit, enabled: Boolean = true) {
    val description = stringResource(R.string.tempo_description, tempo)
    Column(Modifier.fillMaxWidth().semantics { stateDescription = description }, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.tempo_bpm, tempo), style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(R.string.slower, { onChange(tempo - 4) }, enabled = enabled && tempo > 40)
            SecondaryButton(R.string.faster, { onChange(tempo + 4) }, enabled = enabled && tempo < 120)
        }
    }
}
