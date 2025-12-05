package com.swift.newvpn.vpn.boxins

import android.util.Log
import com.swift.newvpn.model.SpeedData
import com.swift.newvpn.model.TrafficData
import com.swift.newvpn.vpn.services.ScapeVpnConnection
import com.swift.newvpn.vpn.services.VpnServiceData
import com.swift.newvpn.vpn.services.VpnState
import com.swift.newvpn.utils.Logs
import com.swift.newvpn.utils.ProxyManager
import com.swift.newvpn.utils.appScope
import com.swift.newvpn.utils.runCalculate
import com.swift.newvpn.vpn.TAG_BYPASS
import com.swift.newvpn.vpn.TAG_PROXY
import kotlinx.coroutines.*
import kotlin.collections.firstOrNull
import kotlin.collections.iterator

class TrafficLooper(
    val data: VpnServiceData, private val sc: CoroutineScope
) {

    private var job: Job? = null
    private val idMap = mutableMapOf<String, TrafficUpdater.TrafficLooperData>() // id to 1 data
    private val tagMap = mutableMapOf<String, TrafficUpdater.TrafficLooperData>() // tag to 1 data

    suspend fun stop() {
        job?.cancel()
        // finally s_traffic post
        val traffic = mutableMapOf<String, TrafficData>()
        data.proxy?.config?.trafficMap?.forEach { (_, ents) ->
            for (ent in ents) {
                val item = idMap[ent.name] ?: return@forEach
                ent.rx = item.rx
                ent.tx = item.tx
                ProxyManager.updateProfile(ent) // update DB
                traffic[ent.name] = TrafficData(
                    name = ent.name,
                    rx = ent.rx,
                    tx = ent.tx,
                )
            }
        }
        data.binder.broadcast { b ->
            for (t in traffic) {
                b.cbTrafficUpdate(t.value)
            }
        }
        Logs.d("finally s_traffic post done")
    }

    fun start() {
        Log.e("TrafficLooper", "start")
        job = sc.launch { loop() }
    }

    var selectorNowId = ""
    var selectorNowFakeTag = ""

    fun selectMain(name: String) {
        Logs.d("select s_traffic count ${TAG_PROXY} to $name, old id is $selectorNowId")
        val oldData = idMap[selectorNowId]
        val newData = idMap[name] ?: return
        oldData?.apply {
            tag = selectorNowFakeTag
            ignore = true
            // post s_traffic when switch
            data.proxy?.config?.trafficMap?.get(tag)?.firstOrNull()?.let {
                it.rx = rx
                it.tx = tx
                appScope.runCalculate {
                    ProxyManager.updateProfile(it) // update DB
                }
            }
        }
        selectorNowFakeTag = newData.tag
        selectorNowId = name
        newData.apply {
            tag = TAG_PROXY
            ignore = false
        }
    }

    private suspend fun loop() {
        val delayMs = 1L
        val showDirectSpeed = true
        val profileTrafficStatistics = true

        var trafficUpdater: TrafficUpdater? = null
        var proxy: ProxyInstance?

        // for display
        val itemBypass = TrafficUpdater.TrafficLooperData(tag = TAG_BYPASS)
        Log.e(TAG_BYPASS, "loop: ${itemBypass}")
        while (sc.isActive) {
            proxy = data.proxy
            if (proxy == null) {
                delay(delayMs)
                continue
            }

            if (trafficUpdater == null) {
                if (!proxy.isInitialized()) continue
                idMap.clear()
                //
                val tags = hashSetOf(TAG_PROXY, TAG_BYPASS)
                proxy.config.trafficMap.forEach { (tag, ents) ->
                    tags.add(tag)
                    for (ent in ents) {
                        val item = TrafficUpdater.TrafficLooperData(
                            tag = tag,
                            rx = ent.rx,
                            tx = ent.tx,
                            rxBase = ent.rx,
                            txBase = ent.tx,
                            ignore = false,
                        )
                        idMap[ent.name] = item
                        tagMap[tag] = item
                        Logs.d("s_traffic count $tag to ${ent.name}")
                    }
                }
                //
                trafficUpdater = TrafficUpdater(
                    box = proxy.box, items = idMap.values.toList()
                )
                proxy.box.setV2rayStats(tags.joinToString("\n"))
            }

            trafficUpdater.updateAll()
            if (!sc.isActive) return

            // add all non-bypass to "main"
            var mainTxRate = 0L
            var mainRxRate = 0L
            var mainTx = 0L
            var mainRx = 0L
            tagMap.forEach { (_, it) ->
                if (!it.ignore) {
                    mainTxRate += it.txRate
                    mainRxRate += it.rxRate
                }
                mainTx += it.tx - it.txBase
                mainRx += it.rx - it.rxBase
            }

            // speed
            val speed = SpeedData(
                mainTxRate,
                mainRxRate,
                if (showDirectSpeed) itemBypass.txRate else 0L,
                if (showDirectSpeed) itemBypass.rxRate else 0L,
                mainTx,
                mainRx
            )

            // broadcast (MainActivity)
            if (data.state == VpnState.Connected
                && data.binder.callbackIdMap.containsValue(ScapeVpnConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND)
            ) {
                data.binder.broadcast { b ->
                    if (data.binder.callbackIdMap[b] == ScapeVpnConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND) {
                        b.cbSpeedUpdate(speed)
                        if (profileTrafficStatistics) {
                            idMap.forEach { (id, item) ->
                                b.cbTrafficUpdate(
                                    TrafficData(name = id, rx = item.rx, tx = item.tx) // display
                                )
                            }
                        }
                    }
                }
            }

            // NotificationHelper
            data.notification?.apply {
                postNotificationSpeedUpdate(speed)
            }

            delay(delayMs)
        }
    }
}