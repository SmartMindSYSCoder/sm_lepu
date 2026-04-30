package com.sm.sm_lepu
import android.content.Context
import android.app.Activity
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.aoj20a.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import android.util.Log
import org.json.JSONObject

class Aoj20aHelper : BaseDeviceHelper() {

    override val model = Bluetooth.MODEL_AOJ20A
    override val tagName = "Aoj20aActivity"
    override val deviceTypeString = "temperature"
    private val name = "Aoj-20a"

    override fun init() {
         super.init()
    }

    override fun initEvents() {
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observeForever  {
                // BleService init success
                BleServiceHelper.BleServiceHelper.startScan(intArrayOf(model))
                Log.d(tagName, "EventServiceConnectedAndInterfaceInit")
            }

        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observeForever  {
                // Cancel checking for timeout
                cancelScanTimeout()

                // set interface before connect
                BleServiceHelper.BleServiceHelper.setInterfaces(it.model)
                val observer = BIOL(this, intArrayOf(it.model))

                // connect
                BleServiceHelper.BleServiceHelper.connect(context, it.model, it.device)
                val deviceModel = it.model
                val deviceName = it.name
                val deviceAddress = it.macAddr

                val isConnected=true
                
                Log.d(tagName, "EventDeviceFound")
                Log.d(tagName, "device name: ${deviceName} \ndevice model: ${deviceModel} \nmac address: ${deviceAddress}")
                val jsonData = JSONObject()
                jsonData.put("deviceType", "temperature")
                jsonData.put("isConnected", isConnected)
                jsonData.put("isCompleted", false)

                jsonData.put("temperature", 0)
                SharedStreamHandler.getInstance().sendEvent(jsonData)
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeviceData)
            .observeForever  {
                val data = it.data as DeviceInfo
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg)
            .observeForever  {
                val data = it.data as ErrorResult
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData)
            .observeForever  {
                val data = it.data as TempResult
                Log.d("temp", "${data.temp}")

                if (data.temp>0) {
                    BleServiceHelper.BleServiceHelper.stopScan()
                    BleServiceHelper.BleServiceHelper.disconnect(false)
                   
                    val jsonData = JSONObject()
                    jsonData.put("deviceType", "temperature")
                    jsonData.put("isConnected", false)
                    jsonData.put("isCompleted", true)
                    jsonData.put("temperature", data.temp.toDouble())
                    SharedStreamHandler.getInstance().sendEvent(jsonData)
                }
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempList)
            .observeForever {
                val data = it.data as ArrayList<Record>
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeleteData)
            .observeForever  {
                val data = it.data as Boolean
            }
    }
}