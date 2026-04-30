package com.sm.sm_lepu
import android.content.Context
import android.app.Activity
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pc60fw.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import android.util.Log
import org.json.JSONObject

class Pc60fwHelper : BaseDeviceHelper() {

    override val model = Bluetooth.MODEL_PC60FW
    override val tagName = "Pc60fwActivity"
    override val deviceTypeString = "spo2"
    private val name = "Pc60fw"

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
        
        // REMOVED duplicate startScan here as per user request to not init/scan until location checked

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
                jsonData.put("deviceType", "spo2")
                jsonData.put("isConnected", isConnected)
                jsonData.put("isCompleted", false)
                jsonData.put("spo2", 0)
                jsonData.put("heart_rate", 0)
                jsonData.put("pi",0)
                SharedStreamHandler.getInstance().sendEvent(jsonData)
            }


        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo)
            .observeForever  {
                val data = it.data as DeviceInfo
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtParam)
            .observeForever {
                val data = it.data as RtParam

                if (data.spo2>0 &&  data.pi>0) {
                    BleServiceHelper.BleServiceHelper.stopScan()
                    BleServiceHelper.BleServiceHelper.disconnect(false)
                   
                    data.isProbeOff=true

                    val jsonData = JSONObject()
                    jsonData.put("deviceType", "spo2")
                    jsonData.put("isConnected", false)
                    jsonData.put("isCompleted", true)
                    jsonData.put("spo2", data.spo2)
                    jsonData.put("heart_rate", data.pr)
                    jsonData.put("pi", data.pi.toDouble())
                    SharedStreamHandler.getInstance().sendEvent(jsonData)
                }
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtWave)
            .observeForever {
                val data = it.data as RtWave
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwBatLevel)
            .observeForever {
                val data = it.data as Int
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwWorkingStatus)
            .observeForever {
                val data = it.data as WorkingStatus
            }
    }
}