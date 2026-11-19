package com.ukulelestrum.school.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.ukulelestrum.school.R
import com.ukulelestrum.school.core.theme.*
import com.ukulelestrum.school.domain.model.StrumPattern
import com.ukulelestrum.school.presentation.common.contentRes

@Composable
fun StrumPatternBoard(pattern: StrumPattern, activeSlot: Int = -1) {
    val spoken = stringResource(contentRes(pattern.spokenKey))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().semantics { contentDescription = spoken }, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            pattern.slots.forEachIndexed { index, direction ->
                val selected = index == activeSlot
                Column(Modifier.weight(1f).background(if (selected) Leaf else Forest.copy(alpha = 0.50f), RoundedCornerShape(8.dp))
                    .border(1.dp, Mist.copy(alpha = if (selected) 0.9f else 0.12f), RoundedCornerShape(8.dp)).padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(when (index) { 0 -> R.string.beat_one; 2 -> R.string.beat_two; 4 -> R.string.beat_three; 6 -> R.string.beat_four; else -> R.string.beat_and }),
                        color = if (selected) Forest else Mist, style = MaterialTheme.typography.labelSmall)
                    Canvas(Modifier.fillMaxWidth().height(40.dp)) {
                        val color = if (selected) Forest else Mist
                        val center = size.width / 2
                        if (direction == 0) drawLine(color.copy(alpha = 0.6f), Offset(center - 5.dp.toPx(), size.height / 2), Offset(center + 5.dp.toPx(), size.height / 2), 2.dp.toPx())
                        else {
                            val from = if (direction > 0) 7.dp.toPx() else size.height - 7.dp.toPx()
                            val to = size.height - from
                            drawLine(color, Offset(center, from), Offset(center, to), 2.dp.toPx())
                            val sign = if (direction > 0) -1 else 1
                            val path = Path().apply { moveTo(center - 5.dp.toPx(), to + sign * 7.dp.toPx()); lineTo(center, to); lineTo(center + 5.dp.toPx(), to + sign * 7.dp.toPx()) }
                            drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                        }
                    }
                }
            }
        }
        Text(spoken, style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(contentRes(pattern.guideKey)), style = MaterialTheme.typography.bodyMedium)
    }
}
