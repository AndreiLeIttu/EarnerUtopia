package com.aospi.earnerutopia

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.content.edit

class BubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: View
    private val handler = Handler(Looper.getMainLooper())
    private var showMessage = false
    private val timeoutSeconds = 2L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        val (lastX, lastY) = getSavedBubblePosition()
        params.x = lastX
        params.y = lastY

        bubbleView = LayoutInflater.from(this).inflate(R.layout.bubble_layout, null)
        windowManager.addView(bubbleView, params)

        val bubbleContainer = bubbleView.findViewById<LinearLayout>(R.id.bubbleContainer)
        val bubbleIcon = bubbleView.findViewById<ImageView>(R.id.bubbleIcon)
        val bubbleText = bubbleView.findViewById<TextView>(R.id.bubbleText)
        fun showTextPopup() {
            val metrics = Resources.getSystem().displayMetrics
            val screenWidth = metrics.widthPixels

            bubbleContainer.removeView(bubbleText)
            val textParams = bubbleText.layoutParams as LinearLayout.LayoutParams

            if (params.x + bubbleIcon.width / 2 < screenWidth / 2) {
                bubbleContainer.addView(bubbleText)
                textParams.marginStart = 10
                textParams.marginEnd = 0
                bubbleText.layoutParams = textParams
            } else {
                bubbleContainer.addView(bubbleText, 0)
                textParams.marginStart = 0
                textParams.marginEnd = 10
                bubbleText.layoutParams = textParams

                bubbleView.post {
                    bubbleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    val circleWidth = bubbleIcon.width

                    params.x = screenWidth - circleWidth
                    windowManager.updateViewLayout(bubbleView, params)
                }

            }
            bubbleText.visibility = View.VISIBLE
        }
        handler.post(object : Runnable {
            override fun run() {
                if (bubbleText.isVisible) {
                    bubbleText.visibility = View.GONE
                    val metrics = Resources.getSystem().displayMetrics
                    val screenWidth = metrics.widthPixels
                    val bubbleWidth = bubbleIcon.width

                    if (params.x != 0 && params.x != screenWidth - bubbleWidth) {
                        params.x = if (params.x + bubbleWidth / 2 < screenWidth / 2) {
                            0
                        } else {
                            screenWidth - bubbleWidth
                        }
                        windowManager.updateViewLayout(bubbleView, params)
                    }

                } else {
                    showTextPopup()
                }

                handler.postDelayed(this, timeoutSeconds * 1000)
            }
        })

        bubbleView.setOnTouchListener(BubbleTouchListener(params, windowManager, bubbleView))
        /*
        bubbleIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            stopSelf()
        }
        */
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::bubbleView.isInitialized) windowManager.removeView(bubbleView)
    }

    private fun getSavedBubblePosition(): Pair<Int, Int> {
        val prefs = getSharedPreferences("bubble_prefs", Context.MODE_PRIVATE)
        val x = prefs.getInt("last_x", 0)
        val y = prefs.getInt("last_y", 500)
        return Pair(x, y)
    }


    private class BubbleTouchListener(
        private val layoutParams: WindowManager.LayoutParams,
        private val windowManager: WindowManager,
        private val bubbleView: View
    ) : View.OnTouchListener {

        private var initialX = 0
        private var initialY = 0
        private var touchX = 0f
        private var touchY = 0f
        private var downTime: Long = 0
        private var isDragging = false
        private val dragThreshold = 200L

        private fun saveBubblePosition(context: Context, x: Int, y: Int) {
            val prefs = context.getSharedPreferences("bubble_prefs", Context.MODE_PRIVATE)
            prefs.edit {
                putInt("last_x", x)
                    .putInt("last_y", y)
            }
        }


        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event == null || v == null) return false

            val metrics = Resources.getSystem().displayMetrics
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels

            val bubbleWidth = v.width.coerceAtLeast(1)
            val bubbleHeight = v.height.coerceAtLeast(1)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    downTime = System.currentTimeMillis()
                    isDragging = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isDragging && (System.currentTimeMillis() - downTime) >= dragThreshold) {
                        isDragging = true
                    }

                    if (isDragging) {
                        layoutParams.x = (initialX + (event.rawX - touchX)).toInt()
                            .coerceIn(0, screenWidth - bubbleWidth)
                        layoutParams.y = (initialY + (event.rawY - touchY)).toInt()
                            .coerceIn(0, screenHeight - bubbleHeight)
                        windowManager.updateViewLayout(bubbleView, layoutParams)
                    }
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val holdTime = System.currentTimeMillis() - downTime
                    if (!isDragging && holdTime < dragThreshold) {
                        val ctx = bubbleView.context
                        val intent = Intent(ctx, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        ctx.startActivity(intent)
                    } else {
                        val targetX = if (layoutParams.x + bubbleWidth / 2 < screenWidth / 2) {
                            0
                        } else {
                            screenWidth - bubbleWidth
                        }
                        val anim = ValueAnimator.ofInt(layoutParams.x, targetX)
                        anim.addUpdateListener { valueAnimator ->
                            layoutParams.x = valueAnimator.animatedValue as Int
                            windowManager.updateViewLayout(bubbleView, layoutParams)
                        }
                        anim.duration = 150
                        anim.start()
                        saveBubblePosition(bubbleView.context, targetX, layoutParams.y)
                    }
                    return true
                }
            }
            return false
        }
    }
}
