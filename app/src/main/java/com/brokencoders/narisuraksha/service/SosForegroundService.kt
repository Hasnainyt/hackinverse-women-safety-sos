package com.brokencoders.narisuraksha.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.brokencoders.narisuraksha.NariSurakshaApp
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.core.SosPacket
import com.brokencoders.narisuraksha.data.SosEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SosForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SosForegroundService onCreate")
        val notificationHelper = (application as NariSurakshaApp).notificationHelper
        val notification = notificationHelper.buildSosBroadcastingNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var fgsType = ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            if (com.brokencoders.narisuraksha.core.PermissionHelper.hasLocationPermission(this)) {
                fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (com.brokencoders.narisuraksha.core.PermissionHelper.hasAudioPermission(this)) {
                    fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                }
            }
            startForeground(Constants.NOTIFICATION_ID_SOS_ACTIVE, notification, fgsType)
        } else {
            startForeground(Constants.NOTIFICATION_ID_SOS_ACTIVE, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SOS) {
            stopSelf()
            return START_NOT_STICKY
        }

        val lat = intent?.getDoubleExtra(EXTRA_LAT, Double.NaN).let { if (it == null || it.isNaN()) null else it }
        val lon = intent?.getDoubleExtra(EXTRA_LON, Double.NaN).let { if (it == null || it.isNaN()) null else it }
        val audioPath = intent?.getStringExtra(EXTRA_AUDIO_PATH)

        broadcastSos(lat, lon, audioPath)

        return START_NOT_STICKY
    }

    private fun broadcastSos(lat: Double?, lon: Double?, audioPath: String?) {
        val app = application as NariSurakshaApp
        val deviceId = app.deviceIdProvider.deviceId
        val bleTransport = app.bleTransport
        val sosDao = app.database.sosDao()

        val packet = SosPacket.create(
            senderId = deviceId,
            lat = lat,
            lon = lon
        )

        Log.i(TAG, "Starting BLE broadcast for packet: $packet")
        try {
            bleTransport.startAdvertising(packet)
        } catch (e: SecurityException) {
            Log.e(TAG, "SOS broadcast aborted: missing BLUETOOTH_ADVERTISE permission", e)
            stopSelf()
            return
        }
        // Ensure device is scanning simultaneously so incoming responder ACKs are captured!
        try {
            bleTransport.startScanning()
        } catch (e: SecurityException) {
            Log.w(TAG, "SOS ACK scan unavailable: missing BLUETOOTH_SCAN permission", e)
        }

        serviceScope.launch {
            // Persist SENT event to Room
            val entity = SosEventEntity(
                eventType = SosEventEntity.TYPE_SENT,
                senderId = deviceId,
                timestamp = System.currentTimeMillis(),
                lat = lat,
                lon = lon,
                audioPath = audioPath,
                rssi = null,
                isLocationUnavailable = (lat == null || lon == null),
                createdAt = System.currentTimeMillis()
            )
            sosDao.insertEvent(entity)

            // Auto-stop service after timeout
            delay(Constants.SOS_ADVERTISE_TIMEOUT_MS)
            Log.i(TAG, "Broadcasting timeout complete. Stopping SosForegroundService.")
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SosForegroundService onDestroy")
        try {
            val app = application as NariSurakshaApp
            app.bleTransport.stopAdvertising()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping advertising in onDestroy", e)
        }
        serviceScope.cancel()
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
            // Pre-O fallback handled by stopSelf below
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "SosForegroundService"
        const val ACTION_START_SOS = "com.brokencoders.narisuraksha.START_SOS"
        const val ACTION_STOP_SOS = "com.brokencoders.narisuraksha.STOP_SOS"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LON = "extra_lon"
        const val EXTRA_AUDIO_PATH = "extra_audio_path"

        fun start(context: Context, lat: Double?, lon: Double?, audioPath: String?) {
            try {
                val intent = Intent(context, SosForegroundService::class.java).apply {
                    action = ACTION_START_SOS
                    lat?.let { putExtra(EXTRA_LAT, it) }
                    lon?.let { putExtra(EXTRA_LON, it) }
                    audioPath?.let { putExtra(EXTRA_AUDIO_PATH, it) }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start SosForegroundService: ${e.message}", e)
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, SosForegroundService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop SosForegroundService: ${e.message}", e)
            }
        }
    }
}
