package com.example.vpn_cubikcode.vpn

import android.content.Intent
import android.net.VpnService
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.example.vpn_cubikcode.data.ConfigRepository
import com.example.vpn_cubikcode.util.JsonValidator
import com.example.vpn_cubikcode.util.VpnEventBus
import com.example.vpn_cubikcode.util.VpnStatus

class MyVpnService : VpnService() {

    private var tunInterface: ParcelFileDescriptor? = null
    private lateinit var xrayManager: XrayManager
    private lateinit var configRepository: ConfigRepository
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        xrayManager = XrayManager(this)
        configRepository = ConfigRepository(this)
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVpn()
            ACTION_STOP -> stopVpn(updateStatus = true)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    override fun onDestroy() {
        stopVpn(updateStatus = false)
        super.onDestroy()
    }

    private fun startVpn() {
        VpnEventBus.setStatus(VpnStatus.CONNECTING)
        VpnEventBus.appendLog("Инициализация VPN...")

        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            notificationHelper.buildNotification("VPN подключается")
        )

        val config = configRepository.getConfig()
        val validation = JsonValidator.validateConfig(config)
        if (validation.isFailure) {
            handleError(validation.exceptionOrNull()?.message ?: "Ошибка валидации JSON")
            return
        }

        val builder = Builder()
            .setSession("VPN CubikCode")
            .addAddress("10.8.0.2", 32)
            .addDnsServer("1.1.1.1")
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)

        val tun = builder.establish()
        if (tun == null) {
            handleError("Не удалось создать TUN интерфейс")
            return
        }

        tunInterface = tun
        VpnEventBus.appendLog("TUN интерфейс создан")

        val startResult = xrayManager.start(config)
        if (startResult.isFailure) {
            handleError(startResult.exceptionOrNull()?.message ?: "Ошибка запуска xray")
            return
        }

        VpnEventBus.setStatus(VpnStatus.CONNECTED)
        VpnEventBus.appendLog("VPN подключен")
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            notificationHelper.buildNotification("VPN подключен")
        )
    }

    private fun stopVpn(updateStatus: Boolean) {
        xrayManager.stop()
        tunInterface?.close()
        tunInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)

        if (updateStatus) {
            VpnEventBus.setStatus(VpnStatus.DISCONNECTED)
            VpnEventBus.appendLog("VPN отключен")
        }
        stopSelf()
    }

    private fun handleError(message: String) {
        VpnEventBus.setStatus(VpnStatus.ERROR)
        VpnEventBus.appendLog(message)
        stopVpn(updateStatus = false)
    }

    companion object {
        const val ACTION_START = "com.example.vpn_cubikcode.action.START_VPN"
        const val ACTION_STOP = "com.example.vpn_cubikcode.action.STOP_VPN"
    }
}
