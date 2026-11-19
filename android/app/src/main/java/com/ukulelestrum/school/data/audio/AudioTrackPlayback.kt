package com.ukulelestrum.school.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.ukulelestrum.school.core.config.GameConfig
import com.ukulelestrum.school.domain.audio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AudioTrackPlayback : AudioPlayback {
    private val mutex = Mutex()
    @Volatile private var track: AudioTrack? = null
    @Volatile private var currentOwner: String? = null

    override suspend fun prepare(request: AudioRequest, volume: Int, owner: String): Long = withContext(Dispatchers.Default) {
        mutex.withLock {
            release()
            val pcm = PcmRenderer.render(request, volume)
            coroutineContext.ensureActive()
            val player = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(GameConfig.SAMPLE_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC).build()
            try {
                check(player.state == AudioTrack.STATE_INITIALIZED)
                check(player.write(pcm, 0, pcm.size) == pcm.size)
                coroutineContext.ensureActive()
                player.play()
                track = player
                currentOwner = owner
                pcm.size.toLong()
            } catch (error: Throwable) {
                player.release()
                throw error
            }
        }
    }
    override fun positionFrames(owner: String): Long = if (currentOwner == owner) runCatching { track?.playbackHeadPosition?.toLong() ?: 0L }.getOrDefault(0) else 0L
    override suspend fun stop(owner: String) = withContext(NonCancellable + Dispatchers.IO) { mutex.withLock { if (currentOwner == owner) release() } }
    private fun release() {
        val player = track
        track = null
        currentOwner = null
        if (player != null) {
            runCatching { player.pause() }
            runCatching { player.flush() }
            player.release()
        }
    }
}
