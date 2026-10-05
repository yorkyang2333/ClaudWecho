package com.yorkyang2333.claudwecho.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import java.lang.ref.WeakReference

fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

object RoundToast {
    private var currentViewRef: WeakReference<View>? = null
    private val handler = Handler(Looper.getMainLooper())
    private var dismissRunnable: Runnable? = null

    @JvmStatic
    fun show(
        context: Context,
        resId: Int,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        show(context, context.getString(resId), duration)
    }

    @JvmStatic
    fun show(
        context: Context,
        message: CharSequence,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        if (message.isEmpty()) return

        val activity = context.findActivity()
        if (activity == null) {
            Toast.makeText(context, message, duration).show()
            return
        }

        handler.post {
            cancelCurrent()

            val wm = activity.windowManager
            val toastView = RoundToastView(activity).apply {
                setText(message)
                translationY = 100f
                alpha = 0f
            }
            currentViewRef = WeakReference(toastView)

            val params = WindowManager.LayoutParams().apply {
                height = WindowManager.LayoutParams.WRAP_CONTENT
                width = WindowManager.LayoutParams.MATCH_PARENT
                format = PixelFormat.TRANSLUCENT
                type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                y = 0
                windowAnimations = 0
            }

            try {
                wm.addView(toastView, params)

                toastView.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setInterpolator(OvershootInterpolator(1.2f))
                    .start()

                val delay = if (duration == Toast.LENGTH_LONG) 3500L else 2000L
                dismissRunnable = Runnable {
                    dismiss(wm, toastView)
                }
                handler.postDelayed(dismissRunnable!!, delay)
            } catch (_: Exception) {
                Toast.makeText(context, message, duration).show()
            }
        }
    }

    private fun dismiss(wm: WindowManager, view: View) {
        view.animate()
            .translationY(100f)
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                try {
                    wm.removeViewImmediate(view)
                } catch (_: Exception) {}
                if (currentViewRef?.get() == view) {
                    currentViewRef = null
                }
            }
            .start()
    }

    private fun cancelCurrent() {
        dismissRunnable?.let { handler.removeCallbacks(it) }
        val view = currentViewRef?.get()
        if (view != null) {
            try {
                val wm = view.context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                wm?.removeViewImmediate(view)
            } catch (_: Exception) {}
            currentViewRef = null
        }
    }
}
