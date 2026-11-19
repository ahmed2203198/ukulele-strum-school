package com.ukulelestrum.school.data.audio

import com.ukulelestrum.school.core.config.GameConfig
import com.ukulelestrum.school.domain.audio.AudioRequest
import com.ukulelestrum.school.domain.model.Chord
import kotlin.math.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

object PcmRenderer {
    private const val rate = GameConfig.SAMPLE_RATE
    private val tuning = listOf(67, 60, 64, 69)
    private fun frequency(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    suspend fun render(request: AudioRequest, volume: Int): ShortArray {
        val samples = when (request) {
            is AudioRequest.Tone -> FloatArray((rate * 1.2).toInt()).also { buffer ->
                addTone(buffer, 0, frequency(request.midi), 1.2, 0.25, 0.015, 0.060, false)
            }
            is AudioRequest.ChordPreview -> FloatArray((rate * 1.6).toInt()).also { buffer -> addChord(buffer, 0, request.chord, 1.6, 0.30) }
            is AudioRequest.Rhythm -> {
                val beat = rate * 60.0 / request.tempo
                val countIn = (beat * 4).roundToInt()
                val chartLength = (beat * 4 * request.chords.size).roundToInt()
                val start = request.startFrame.toInt().coerceIn(0, chartLength)
                val buffer = FloatArray(countIn + chartLength - start)
                repeat(4) { index -> addClick(buffer, (index * beat).roundToInt(), index == 0) }
                repeat(request.chords.size * 4) { index ->
                    currentCoroutineContext().ensureActive()
                    val frame = (index * beat).roundToInt()
                    if (frame >= start) addClick(buffer, countIn + frame - start, index % 4 == 0)
                }
                repeat(request.chords.size * 8) { slot ->
                    currentCoroutineContext().ensureActive()
                    val frame = (slot * beat / 2).roundToInt()
                    val direction = request.pattern.slots[slot % 8]
                    if (frame >= start && direction != 0) addChord(buffer, countIn + frame - start,
                        request.chords[slot / 8], 0.18, if (direction > 0) 0.18 else 0.12)
                }
                buffer
            }
        }
        val scale = volume.coerceIn(0, 100) / 100.0
        return ShortArray(samples.size) { index -> (samples[index].coerceIn(-1f, 1f) * scale * Short.MAX_VALUE).toInt().toShort() }
    }
    private fun addClick(buffer: FloatArray, start: Int, strong: Boolean) {
        addTone(buffer, start, if (strong) 1000.0 else 700.0, if (strong) 0.030 else 0.024, 0.20, 0.004, 0.010, false)
    }
    private fun addChord(buffer: FloatArray, start: Int, chord: Chord, seconds: Double, peak: Double) {
        chord.frets.forEachIndexed { index, fret -> addTone(buffer, start, frequency(tuning[index] + fret), seconds, peak / 4, 0.010, 0.030, true) }
    }
    private fun addTone(buffer: FloatArray, start: Int, hz: Double, seconds: Double, peak: Double, attack: Double, release: Double, decay: Boolean) {
        val length = (rate * seconds).toInt()
        for (i in 0 until min(length, buffer.size - start).coerceAtLeast(0)) {
            val time = i.toDouble() / rate
            val envelope = min(1.0, time / attack) * min(1.0, (seconds - time) / release) * if (decay) exp(-3.0 * time / seconds) else 1.0
            buffer[start + i] += (sin(2 * PI * hz * time) * peak * envelope).toFloat()
        }
    }
}
