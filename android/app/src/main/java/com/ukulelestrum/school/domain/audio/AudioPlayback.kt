package com.ukulelestrum.school.domain.audio

import com.ukulelestrum.school.domain.model.Chord
import com.ukulelestrum.school.domain.model.StrumPattern

sealed interface AudioRequest {
    data class Tone(val midi: Int) : AudioRequest
    data class ChordPreview(val chord: Chord) : AudioRequest
    data class Rhythm(val chords: List<Chord>, val pattern: StrumPattern, val tempo: Int, val startFrame: Long = 0) : AudioRequest
}

interface AudioPlayback {
    suspend fun prepare(request: AudioRequest, volume: Int, owner: String): Long
    fun positionFrames(owner: String): Long
    suspend fun stop(owner: String)
}
