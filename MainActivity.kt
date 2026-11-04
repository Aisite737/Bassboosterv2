package com.example.bassboosterpro

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.drawable.GradientDrawable

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("bass", MODE_PRIVATE) }
    private var amount = 65
    private var enabled = false
    private lateinit var value: TextView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        amount = prefs.getInt("amount", 65)
        enabled = prefs.getBoolean("enabled", false)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9)
        }
        buildUi()
        if (enabled) startBass()
    }

    private fun buildUi() {
        val bg = Color.rgb(10, 14, 24)
        val cyan = Color.rgb(48, 210, 255)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 42, 28, 28)
            setBackgroundColor(bg)
        }
        val title = TextView(this).apply {
            text = "BASS BOOSTER PRO"
            textSize = 23f; setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            letterSpacing = .08f
        }
        root.addView(title)
        val sub = TextView(this).apply {
            text = "REAL-TIME LOW FREQUENCY"
            textSize = 11f; setTextColor(cyan); letterSpacing = .18f
            setPadding(0, 8, 0, 34)
        }
        root.addView(sub)

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(22, 28, 22, 24)
            background = GradientDrawable().apply {
                setColor(Color.rgb(20, 28, 43)); cornerRadius = 28f
                setStroke(1, Color.rgb(38, 67, 91))
            }
        }
        value = TextView(this).apply {
            text = "$amount%"
            textSize = 48f; setTextColor(cyan)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        panel.addView(value)
        panel.addView(TextView(this).apply {
            text = "BASS POWER"; textSize = 11f; setTextColor(0xFF9BAFC4.toInt())
            letterSpacing = .2f
            setPadding(0, 0, 0, 20)
        })
        val seek = SeekBar(this).apply {
            max = 100; progress = amount
            progressTintList = android.content.res.ColorStateList.valueOf(cyan)
            thumbTintList = android.content.res.ColorStateList.valueOf(cyan)
        }
        panel.addView(seek, LinearLayout.LayoutParams(-1, 48))
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) {
                amount = p; value.text = "$p%"
                if (enabled) updateBass()
                prefs.edit().putInt("amount", p).apply()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        root.addView(panel, LinearLayout.LayoutParams(-1, -2))

        val toggle = Button(this).apply {
            text = if (enabled) "BASS BOOST: ON" else "BASS BOOST: OFF"
            isAllCaps = false; textSize = 16f
            setTextColor(Color.BLACK)
            background = GradientDrawable().apply {
                setColor(cyan); cornerRadius = 22f
            }
            setPadding(12, 18, 12, 18)
            setOnClickListener {
                enabled = !enabled
                prefs.edit().putBoolean("enabled", enabled).apply()
                if (enabled) startBass() else stopBass()
                text = if (enabled) "BASS BOOST: ON" else "BASS BOOST: OFF"
                status.text = if (enabled) "Пытаемся включить системный аудиоэффект" else "Эффект выключен"
            }
        }
        val lp = LinearLayout.LayoutParams(-1, -2).apply { topMargin = 24 }
        root.addView(toggle, lp)

        status = TextView(this).apply {
            text = if (enabled) "Бас включён (если поддерживается прошивкой)" else "Готов к работе"
            textSize = 12f; setTextColor(0xFF9BAFC4.toInt())
            gravity = Gravity.CENTER; setPadding(8, 18, 8, 0)
        }
        root.addView(status)
        root.addView(TextView(this).apply {
            text = "Совет: начни с 30–50%. Слишком сильный бас может вызвать искажения."
            textSize = 12f; setTextColor(0xFF71849A.toInt())
            gravity = Gravity.CENTER; setPadding(10, 26, 10, 0)
        })
        setContentView(root)
    }

    private fun startBass() {
        val i = Intent(this, BassService::class.java).putExtra("amount", amount)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
    }
    private fun updateBass() {
        startService(Intent(this, BassService::class.java).putExtra("amount", amount))
    }
    private fun stopBass() {
        stopService(Intent(this, BassService::class.java))
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}
