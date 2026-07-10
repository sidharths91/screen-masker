package com.noxvix.screenmask

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val overlayViews = mutableListOf<View>()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundService()
        applyMasks()
    }

    private fun applyMasks() {
        // Dynamic slider margins compiled by the builder tool
        val leftWidth = 25
        val rightWidth = 0
        val topHeight = 0
        val bottomHeight = 0
        val maskColor = Color.BLACK

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // FLAG_NOT_FOCUSABLE allows normal interactions, FLAG_NOT_TOUCH_MODAL passes background touches,
        // and crucially FLAG_NOT_TOUCHABLE lets the user touch through the mask seamlessly.
        val touchFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        // 1. Create Left Bezel Mask
        if (leftWidth > 0) {
            val paramsLeft = WindowManager.LayoutParams(
                leftWidth,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                touchFlags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.TOP
                x = 0
                y = 0
            }
            createOverlayView(paramsLeft, maskColor)
        }

        // 2. Create Right Bezel Mask
        if (rightWidth > 0) {
            val paramsRight = WindowManager.LayoutParams(
                rightWidth,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                touchFlags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.END or Gravity.TOP
                x = 0
                y = 0
            }
            createOverlayView(paramsRight, maskColor)
        }

        // 3. Create Top Bezel Mask
        if (topHeight > 0) {
            val paramsTop = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                topHeight,
                layoutFlag,
                touchFlags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.TOP
                x = 0
                y = 0
            }
            createOverlayView(paramsTop, maskColor)
        }

        // 4. Create Bottom Bezel Mask
        if (bottomHeight > 0) {
            val paramsBottom = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                bottomHeight,
                layoutFlag,
                touchFlags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.BOTTOM
                x = 0
                y = 0
            }
            createOverlayView(paramsBottom, maskColor)
        }
    }

    private fun createOverlayView(params: WindowManager.LayoutParams, color: Int) {
        val view = View(this).apply {
            setBackgroundColor(color)
        }
        windowManager.addView(view, params)
        overlayViews.add(view)
    }

    private fun startForegroundService() {
        val channelId = "screen_masker_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Screen Mask Active Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Screen Mask Active")
            .setContentText("Your customized margins are active.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        for (view in overlayViews) {
            try {
                windowManager.removeView(view)
            } catch (e: Exception) { }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
