package com.example.bassboosterpro

import android.app.*
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.IBinder

class BassService : Service() {
    private var bass: BassBoost? = null
    private var eq: Equalizer? = null
    private val channel = "bass_effects"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(channel, "Bass effect", NotificationManager.IMPORTANCE_LOW))
        }
        val notification = Notification.Builder(this, if (Build.VERSION.SDK_INT >= 26) channel else "")
            .setContentTitle("Bass Booster Pro")
            .setContentText("Управление бас-эффектом активно")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true).build()
        startForeground(71, notification)
        createEffects()
    }

    private fun createEffects() {
        try {
            if (bass == null) bass = BassBoost(0, 0)
            if (eq == null) eq = Equalizer(0, 0)
            bass?.enabled = true
            eq?.enabled = true
        } catch (_: Exception) {
            // Some Android builds block global output-mix effects.
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val amount = (intent?.getIntExtra("amount", 65) ?: 65).coerceIn(0, 100)
        try {
            createEffects()
            bass?.setStrength((amount * 10).toShort())
            eq?.let { equalizer ->
                val range = equalizer.bandLevelRange
                for (band in 0 until equalizer.numberOfBands) {
                    val hz = equalizer.getCenterFreq(band.toShort()) / 1000
                    if (hz <= 250) {
                        val strength = (range[1] * amount / 100).coerceIn(range[0].toInt(), range[1].toInt())
                        equalizer.setBandLevel(band.toShort(), strength.toShort())
                    }
                }
            }
        } catch (_: Exception) {
            // Device/firmware may not expose a global equalizer session.
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try { bass?.release() } catch (_: Exception) {}
        try { eq?.release() } catch (_: Exception) {}
        bass = null; eq = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
