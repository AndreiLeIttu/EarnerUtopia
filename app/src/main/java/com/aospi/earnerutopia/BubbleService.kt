package com.aospi.earnerutopia

import android.Manifest
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.aospi.earnerutopia.weather.WeatherClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat


class BubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: View
    private val handler = Handler(Looper.getMainLooper())
    private val weatherApi = WeatherClient.api
    private var showMessage = false
    private val breakTimeoutSeconds = 60L
    private val breakDurationSeconds = 5L
    private val weatherCheckTimeoutSeconds = 120L
    private val weatherWarningSeconds = 5L
    private val badWeatherKeywords = listOf("Heavy", "Thunderstorm", "Snow", "Drizzle", "Extreme", "Freezing", "Very", "Smoke", "Fog", "Volcanic")
    private val API_KEY = BuildConfig.OPENWEATHER_API_KEY
    private lateinit var bubbleParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    private fun attachBubbleView(params: WindowManager.LayoutParams) {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager

        if (!Settings.canDrawOverlays(this)) {
            Log.e("BubbleService", "Overlay permission missing, requesting...")
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return
        }

        Handler(Looper.getMainLooper()).post {
            try {
                if (!::bubbleView.isInitialized) {
                    bubbleView = LayoutInflater.from(this).inflate(R.layout.bubble_layout, null)
                }

                if (!bubbleView.isAttachedToWindow) {
                    wm.addView(bubbleView, params)
                    Log.d("BubbleService", "BubbleView attached")
                } else {
                    Log.d("BubbleService", "BubbleView already attached")
                }
            } catch (e: Exception) {
                Log.e("BubbleService", "Failed to attach bubbleView: ${e.message}")
            }
        }
    }

    private fun showBubblePopup(params: WindowManager.LayoutParams, text: String, durationSeconds: Long) {
        if (!::bubbleView.isInitialized || !bubbleView.isAttachedToWindow) return

        val bubbleText = bubbleView.findViewById<TextView>(R.id.bubbleText)
        val bubbleContainer = bubbleView.findViewById<LinearLayout>(R.id.bubbleContainer)
        val bubbleIcon = bubbleView.findViewById<ImageView>(R.id.bubbleIcon)
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        bubbleContainer.removeView(bubbleText)
        val textParams = bubbleText.layoutParams as LinearLayout.LayoutParams
        val bubbleCenter = params.x + bubbleIcon.width / 2
        if (bubbleCenter < screenWidth / 2) {
            bubbleContainer.addView(bubbleText)
            textParams.marginStart = 10
            textParams.marginEnd = 0
            bubbleText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        } else {
            bubbleContainer.addView(bubbleText, 0)
            textParams.marginStart = 0
            textParams.marginEnd = 10
            bubbleText.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        }
        bubbleText.layoutParams = textParams

        bubbleText.text = text
        bubbleText.visibility = View.VISIBLE

        handler.postDelayed({
            bubbleText.visibility = View.GONE
        }, durationSeconds * 1000)
    }

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

        if (!bubbleView.isAttachedToWindow) {
            windowManager.addView(bubbleView, params)
        }

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        bubbleParams.gravity = Gravity.TOP or Gravity.START
        bubbleParams.x = lastX
        bubbleParams.y = lastY
        attachBubbleView(bubbleParams)

        startAsForeground()

        val metrics = Resources.getSystem().displayMetrics
        val screenWidth = metrics.widthPixels

        val bubbleContainer = bubbleView.findViewById<LinearLayout>(R.id.bubbleContainer)
        val bubbleIcon = bubbleView.findViewById<ImageView>(R.id.bubbleIcon)
        val bubbleText = bubbleView.findViewById<TextView>(R.id.bubbleText)

        fun showTextPopup() {
            bubbleContainer.removeView(bubbleText)
            val textParams = bubbleText.layoutParams as LinearLayout.LayoutParams

            val bubbleCenter = params.x + bubbleIcon.width / 2

            if (bubbleCenter < screenWidth / 2) {
                bubbleContainer.addView(bubbleText)
                textParams.marginStart = 10
                textParams.marginEnd = 0
                bubbleText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            } else {
                bubbleContainer.addView(bubbleText, 0)
                textParams.marginStart = 0
                textParams.marginEnd = 10
                bubbleText.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            }

            bubbleText.layoutParams = textParams
            bubbleText.visibility = View.VISIBLE
        }

        handler.post(object : Runnable {
            override fun run() {
                showBubblePopup(params, "It's been 2 hours. Please don't forget to take a break.", breakDurationSeconds)
                handler.postDelayed(this, breakTimeoutSeconds * 1000)
            }
        })

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("Location", "lat = ${location.latitude}, long = ${location.longitude}")
                    startWeatherChecks(location.latitude, location.longitude)
                }
            }
        } else {
            Log.d("BubbleService", "Location permission not granted, cannot fetch weather")
        }


        bubbleView.setOnTouchListener(BubbleTouchListener(params, windowManager, bubbleView))
    }
    private fun startAsForeground() {
        val channelId = "bubble_channel"

        val channel = NotificationChannel(
            channelId,
            "Bubble Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Bubble active")
            .setContentText("Needed for weather updates and reminders while you work")
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (::bubbleView.isInitialized) windowManager.removeView(bubbleView)
    }

    private fun getSavedBubblePosition(): Pair<Int, Int> {
        val prefs = getSharedPreferences("bubble_prefs", Context.MODE_PRIVATE)
        val x = prefs.getInt("last_x", 0)
        val y = prefs.getInt("last_y", 500)
        return Pair(x, y)
    }
    private fun saveBubblePosition(context: Context, x: Int, y: Int) {
        val prefs = context.getSharedPreferences("bubble_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putInt("last_x", x)
                .putInt("last_y", y)
        }
    }

    inner class BubbleTouchListener(
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
        private val metrics = Resources.getSystem().displayMetrics
        private val screenWidth = metrics.widthPixels
        private val screenHeight = metrics.heightPixels

        private fun updateBubbleTextPosition() {
            val bubbleText = bubbleView.findViewById<TextView>(R.id.bubbleText)
            val bubbleContainer = bubbleView.findViewById<LinearLayout>(R.id.bubbleContainer)
            val bubbleIcon = bubbleView.findViewById<ImageView>(R.id.bubbleIcon)

            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val bubbleCenter = layoutParams.x + bubbleIcon.width / 2

            bubbleContainer.removeView(bubbleText)
            val textParams = bubbleText.layoutParams as LinearLayout.LayoutParams

            if (bubbleCenter < screenWidth / 2) {
                bubbleContainer.addView(bubbleText)
                textParams.marginStart = 10
                textParams.marginEnd = 0
                bubbleText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            } else {
                bubbleContainer.addView(bubbleText, 0)
                textParams.marginStart = 0
                textParams.marginEnd = 10
                bubbleText.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            }

            bubbleText.layoutParams = textParams
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event == null || v == null) return false
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
                        updateBubbleTextPosition()
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
                        anim.duration = 50
                        anim.start()
                        saveBubblePosition(bubbleView.context, targetX, layoutParams.y)
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun updateBubbleText(text: String) {

        if (!::bubbleView.isInitialized) {
            Log.d("BubbleService", "Bubble not attached, cannot show text $text")
            return
        }
        val bubbleText = bubbleView.findViewById<TextView>(R.id.bubbleText)
        bubbleText.bringToFront()
        if (::bubbleView.isInitialized && bubbleView.isAttachedToWindow) {
            Log.d("BubbleService", "Bubble is attached, updating text")
        } else {
            Log.d("BubbleService", "Bubble NOT attached, cannot show text")
        }
        bubbleText.post {
            bubbleText.text = text
            bubbleText.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
            Log.d("BubbleService", "Showing text: $text")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        if (network == null) {
            Log.d("Weather", "No active network")
            return false
        }

        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities == null) {
            Log.d("Weather", "No network capabilities")
            return false
        }

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        Log.d("Weather", "Has internet capability: $hasInternet")
        return hasInternet
    }

    private fun startWeatherChecks(lat: Double, lon: Double) {
        handler.post(object : Runnable {
            override fun run() {
                /*
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = weatherApi.getHourlyForecast(
                            lat = lat,
                            lon = lon,
                            apiKey = API_KEY
                        )

                        val nextHours = response.hourly.take(2)
                        Log.d("WeatherResponse", nextHours[0].toString())

                        val warning = nextHours.find { hour ->
                            hour.weather.any { it.main.lowercase() in badWeatherKeywords.map { kw -> kw.lowercase() } }
                        }

                        if (warning != null) {
                            val desc = warning.weather[0].description
                            updateBubbleText("Bad weather alert: $desc")

                            handler.postDelayed({
                                updateBubbleText("")
                            }, weatherWarningSeconds * 1000)
                        } else {
                            updateBubbleText("")
                        }

                    } catch (e: Exception) {
                        Log.e("BubbleService", "Weather fetch failed: ${e.message}")
                    }
                }
                */
                // ---- REPLACE API CALL WITH HARDCODED TEST ----
                data class WeatherInfo(val main: String, val description: String)
                data class HourlyWeather(val weather: List<WeatherInfo>)
                val nextHours = listOf(
                    HourlyWeather(listOf(WeatherInfo("heavy", "Heavy rain coming"))),  // lowercase
                    HourlyWeather(listOf(WeatherInfo("CLEAR", "Sunny skies")))          // uppercase
                )
                /*
                val warning = nextHours.find { hour ->
                    hour.weather.any { it.main.lowercase() in badWeatherKeywords.map { kw -> kw.lowercase() } }
                }

                if (warning != null) {
                    val desc = warning.weather[0].description
                    handler.postDelayed({
                        updateBubbleText("Bad weather alert: $desc")

                        handler.postDelayed({
                            updateBubbleText("")
                        }, weatherWarningSeconds * 1000)
                    }, 3000)
                } else {
                    updateBubbleText("")
                }

                handler.postDelayed(this, weatherCheckTimeoutSeconds * 1000L)

                 */
                val warning = nextHours.find { hour ->
                    hour.weather.any { it.main.lowercase() in badWeatherKeywords.map { kw -> kw.lowercase() } }
                }
                warning?.let {
                    showBubblePopup(bubbleParams, "Bad weather alert: ${it.weather[0].description}", weatherWarningSeconds)
                }
                handler.postDelayed(this, weatherCheckTimeoutSeconds * 1000L)
            }
        })
    }

}
