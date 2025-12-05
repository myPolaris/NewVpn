package com.swift.newvpn.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.pm.ServiceInfo
import android.text.format.Formatter
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.swift.newvpn.R
import com.swift.newvpn.base.Constants
import com.swift.newvpn.model.SocksBean
import com.swift.newvpn.model.SpeedData
import com.swift.newvpn.vpn.receiver.ScreenStateReceiver
import com.swift.newvpn.vpn.services.ScapeVpnInterface
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotificationHelper(
    private val service: ScapeVpnInterface, title: String,
    channel: String, visible: Boolean = false,
) {
    companion object {
        const val notificationId = 1

        fun genTitle(ent: SocksBean): String {
            return ent.displayName()
        }

        fun updateNotificationChannels() {
            if (isAndroid8()) {
                notificationManager?.createNotificationChannels(
                    listOf(
                        NotificationChannel(
                            Constants.NOTIFICATION_CHANNEL,
                            Constants.NOTIFICATION_NAME,
                            if (isAndroid9()) NotificationManager.IMPORTANCE_MIN
                            else NotificationManager.IMPORTANCE_LOW
                        ),
                    )
                )
            }
        }
    }

    private val showDirectSpeed = true

    private val builder = NotificationCompat.Builder(service as Context, channel)
        .setWhen(0)
        .setTicker(service.getString(R.string.s_forward_success))
        .setContentTitle(title)
        .setOnlyAlertOnce(true)
        .setContentIntent(AppUtils.configureIntent(service))
        .setSmallIcon(R.drawable.ic_small_launcher_round)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setPriority(if (visible) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MIN)

    private val buildLock = Mutex()
    private val screenStateReceiver: ScreenStateReceiver

    init {
        service as Context
        builder.color = service.getColorAttr(android.R.attr.colorPrimary)
        screenStateReceiver = ScreenStateReceiver(service).register()
        service.serviceScope.runMain {
            show()
        }
    }


    fun formatSpeed(speed: Long) = app.formatFileSize(speed)

    suspend fun postNotificationSpeedUpdate(stats: SpeedData) {
      if (screenStateReceiver.listenPostSpeed){
          useBuilder {
              if (showDirectSpeed) {
                  val speedDetail = R.string.s_speed_detail.getString(
                      formatSpeed(stats.txRateProxy),
                      formatSpeed(stats.rxRateProxy),
                      formatSpeed(stats.txRateDirect),
                      formatSpeed(stats.rxRateDirect)
                  )
                  it.setStyle(NotificationCompat.BigTextStyle().bigText(speedDetail))
                  it.setContentText(speedDetail)
              } else {
                  val speedSimple = R.string.s_traffic.getString(
                      formatSpeed(stats.txRateProxy),
                      formatSpeed(stats.rxRateProxy)
                  )
                  it.setContentText(speedSimple)
              }
              it.setSubText(
                  R.string.s_traffic.getString(
                      Formatter.formatFileSize(app, stats.txTotal),
                      Formatter.formatFileSize(app, stats.rxTotal)
                  )
              )
          }
          update()
      }
    }

    suspend fun postNotificationTitle(newTitle: String) {
        useBuilder {
            it.setContentTitle(newTitle)
        }
        update()
    }

    suspend fun postNotificationWakeLockStatus(acquired: Boolean) {
        useBuilder {
            it.priority =
                if (acquired) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW
        }
        update()
    }


    private suspend fun useBuilder(f: (NotificationCompat.Builder) -> Unit) {
        buildLock.withLock {
            f(builder)
        }
    }

    private suspend fun show() =
        useBuilder {
            runCatching {
                (service as Service).run {
                    if (isAndroid14()) {
                        startForeground(
                            notificationId,
                            it.build(),
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
                        )
                    } else {
                        startForeground(notificationId, it.build())
                    }
                }
            }.onFailure { e ->
                AppUtils.showToast(app, "startForeground: $e")
            }
        }

    private suspend fun update() = useBuilder {
        NotificationManagerCompat.from(service as Service).notify(notificationId, it.build())
    }

    fun destroy() {
        (service as Service).stopForeground(Service.STOP_FOREGROUND_REMOVE)
        screenStateReceiver.destroy()
    }
}