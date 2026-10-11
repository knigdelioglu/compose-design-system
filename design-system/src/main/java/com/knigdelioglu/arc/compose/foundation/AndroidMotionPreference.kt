package com.knigdelioglu.arc.compose.foundation

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Android animator-scale preference used to honor the user's reduced-motion setting. */
object AndroidMotionPreference {
    fun shouldReduce(animatorDurationScale: Float): Boolean =
        !animatorDurationScale.isFinite() || animatorDurationScale <= 0f

    fun read(resolver: ContentResolver): Boolean {
        val scale = runCatching {
            Settings.Global.getFloat(
                resolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }.getOrDefault(1f)
        return shouldReduce(scale)
    }
}

@Composable
fun rememberSystemReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    var reduced by remember(resolver) {
        mutableStateOf(AndroidMotionPreference.read(resolver))
    }

    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduced = AndroidMotionPreference.read(resolver)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        reduced = AndroidMotionPreference.read(resolver)
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return reduced
}
