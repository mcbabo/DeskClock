package app.grapheneos.deskclock.core.util

import android.app.Service
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Collects the given one-off [effect] flow only while the activity is at least in the
 * [Lifecycle.State.STARTED] state, forwarding every emission to [onEffect].
 */
fun <T> ComponentActivity.collectEffectsOnStart(effect: Flow<T>, onEffect: (T) -> Unit) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effect.collect(onEffect)
        }
    }
}

/**
 * Stops the given background [serviceClass] and finishes this activity, a common pattern used
 * by alert pop-up screens once the user dismisses or stops the alarm/timer.
 */
fun ComponentActivity.stopServiceAndFinish(serviceClass: Class<out Service>) {
    stopService(Intent(this, serviceClass))
    finish()
}
