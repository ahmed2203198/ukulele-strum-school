package com.ukulelestrum.school.presentation.common

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun PauseOnLeave(onPause: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val callback by rememberUpdatedState(onPause)
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) callback() }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            callback()
        }
    }
}
