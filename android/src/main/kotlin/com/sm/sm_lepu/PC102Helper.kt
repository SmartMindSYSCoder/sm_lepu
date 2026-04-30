package com.sm.sm_lepu
import android.content.Context
import android.app.Activity
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.pc102.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import android.util.Log
import org.json.JSONObject

class PC102Helper : BaseDeviceHelper() {

    override val model = Bluetooth.MODEL_PC100
    override val tagName = "PC102Activity"
    override val deviceTypeString = "bloodPressure"
    private val name = "pc100"

    override fun init() {
         super.init()
    }

    public fun startBP(){
        BleServiceHelper.BleServiceHelper.pc100StartBp(model)
    }

    public fun stopBP(){
        BleServiceHelper.BleServiceHelper.pc100StopBp(model)
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
                jsonData.put("deviceType", "bloodPressure")
                jsonData.put("isConnected", isConnected)
                jsonData.put("isCompleted", false)
                jsonData.put("systolic", "0")
                jsonData.put("diastolic", "0")
                jsonData.put("heart_rate", "0")
                jsonData.put("progress", "0")
                jsonData.put("isCompleted", false)

                SharedStreamHandler.getInstance().sendEvent(jsonData)
            }


        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo)
            .observeForever {
                val data = it.data as DeviceInfo
               // binding.dataLog.text = "$data"
            }


        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady)
            .observeForever {
                startBP();
            }


        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtBpData)
            .observeForever {
                val data = it.data as RtBpData

                val jsonData = JSONObject()
                jsonData.put("deviceType", "bloodPressure")
                jsonData.put("isConnected", true)
                jsonData.put("isCompleted", false)

                jsonData.put("systolic", 0)
                jsonData.put("diastolic", 0)
                jsonData.put("heart_rate", 0)
                jsonData.put("progress", data.ps.toDouble())

                SharedStreamHandler.getInstance().sendEvent(jsonData)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observeForever {
                val data = it.data as BpResult

                if (data.sys >0 && data.dia >0) {
                    stopBP();

                    dispose();

                    val jsonData = JSONObject()
                    jsonData.put("deviceType", "bloodPressure")
                    jsonData.put("isConnected", true)
                    jsonData.put("isCompleted", true)
                    jsonData.put("systolic", data.sys)
                    jsonData.put("diastolic", data.dia)
                    jsonData.put("heart_rate", data.pr)
                    jsonData.put("progress",0)

                    SharedStreamHandler.getInstance().sendEvent(jsonData)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observeForever{
                val data = it.data as BpResultError
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyParam)
            .observeForever {
                val data = it.data as RtOxyParam
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100RtOxyWave)
            .observeForever {
                val data = it.data as RtOxyWave
            }
    }
}