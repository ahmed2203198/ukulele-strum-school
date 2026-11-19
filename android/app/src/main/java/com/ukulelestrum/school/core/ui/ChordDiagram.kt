package com.ukulelestrum.school.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.ukulelestrum.school.R
import com.ukulelestrum.school.core.theme.*
import com.ukulelestrum.school.domain.model.Chord
import com.ukulelestrum.school.presentation.common.contentRes

@Composable
fun ChordDiagram(chord: Chord) {
    val description = stringResource(contentRes(chord.descriptionKey))
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(contentRes(chord.nameKey)), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.string_order), style = MaterialTheme.typography.labelSmall)
        Column(Modifier.width(188.dp).semantics(mergeDescendants = true) { contentDescription = description }) {
            Row(Modifier.width(160.dp)) {
                listOf(R.string.string_g, R.string.string_c, R.string.string_e, R.string.string_a).forEach { label ->
                    Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) { Text(stringResource(label), style = MaterialTheme.typography.labelSmall) }
                }
            }
            Box(Modifier.width(188.dp).height(184.dp)) {
                Canvas(Modifier.width(160.dp).height(184.dp)) {
                    val unit = 1.dp.toPx()
                    repeat(4) { index ->
                        val x = (20 + index * 40) * unit
                        drawLine(Mist.copy(alpha = 0.65f), Offset(x, 16 * unit), Offset(x, 168 * unit), 1.3f * unit)
                        if (chord.frets[index] == 0) drawCircle(Mist, 4 * unit, Offset(x, 5 * unit), style = Stroke(1.5f * unit))
                    }
                    repeat(5) { index ->
                        val y = (16 + index * 38) * unit
                        drawLine(Mist.copy(alpha = 0.65f), Offset(20 * unit, y), Offset(140 * unit, y), (if (index == 0) 4 else 1) * unit)
                    }
                    chord.frets.forEachIndexed { index, fret ->
                        if (fret > 0) drawCircle(Leaf, 12 * unit, Offset((20 + index * 40) * unit, (16 + (fret - 0.5f) * 38) * unit))
                    }
                }
                chord.frets.forEachIndexed { index, fret ->
                    if (fret > 0) Box(Modifier.offset((8 + index * 40).dp, (4 + (fret - 0.5f) * 38).dp).size(24.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.finger_number, chord.fingers[index]), color = Forest, style = MaterialTheme.typography.labelSmall)
                    }
                }
                repeat(4) { index ->
                    Text(stringResource(R.string.fret_number, index + 1), Modifier.offset(166.dp, (24 + index * 38).dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(description, style = MaterialTheme.typography.bodyMedium)
    }
}
