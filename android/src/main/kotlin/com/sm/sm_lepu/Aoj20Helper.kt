package com.sm.sm_lepu
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.app.Activity
import android.app.Application

import android.content.pm.PackageManager
//import android.icu.text.SimpleDateFormat
import android.location.LocationManager

import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.aoj20a.*
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import android.os.Build

//import com.sm.lepu_sdk.utils.*
import java.util.*

import kotlin.collections.ArrayList
import android.util.Log
import android.widget.Toast
import io.flutter.plugin.common.EventChannel
import org.json.JSONObject

class Aoj20aHelper:BleChangeObserver {


    var isConnected: Boolean =false




    lateinit  var context: Context
    lateinit  var activity: Activity
  //  lateinit var lifecycleOwner: LifecycleOwner

    private val TAG = "Aoj20aActivity"

    private val model = Bluetooth.MODEL_AOJ20A
    private val name = "Aoj-20a"
    private var list = arrayListOf<Bluetooth>()

//    lateinit var result: MethodChannel.Result

    fun initialize(activity: Activity,context: Context){
        this.context=context
        this.activity=activity

        //this.lifecycleOwner=activity.  lifecycle
    }


    public fun init(){

        initEventBus()

        checkBt()

    }



    private fun initEventBus() {



        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observeForever  {
                // Toast.makeText(this,"Start Scan",Toast.LENGTH_SHORT).show()

                // BleService init success
                BleServiceHelper.BleServiceHelper.startScan(intArrayOf(model))
                Log.d(TAG, "EventServiceConnectedAndInterfaceInit")
            }

        BleServiceHelper.BleServiceHelper.startScanByName(name,model)

        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observeForever  {

                //   Toast.makeText(this,it.name,Toast.LENGTH_SHORT).show()

                // set interface before connect
                BleServiceHelper.BleServiceHelper.setInterfaces(it.model)
                // add observer(ble state)
//              activity.  lifecycle.addObserver(BIOL(this, intArrayOf(it.model)))
//                BIOL(this, intArrayOf(it.model)).let { observer ->
//                    BleServiceHelper.BleServiceHelper.registerObserver(observer)
//                }
                // stop scan before connect

                val observer = BIOL(this, intArrayOf(it.model))

                // If needed, start observing BLE state or directly use the observer for any callbacks
               // observer.onBleStateChanged(it.model, Ble.State.CONNECTED)



                // connect
                BleServiceHelper.BleServiceHelper.connect(context, it.model, it.device)
                deviceModel = it.model
                deviceName = it.name
                deviceAddress = it.macAddr
//                ble_name.text = deviceName

                isConnected=true
                // scan result
                //  splitDevices(ble_split.text.toString())
                Log.d(TAG, "EventDeviceFound")
                Log.d(TAG, "device name: ${deviceName} \ndevice model: ${deviceModel} \nmac address: ${deviceAddress}")
                val jsonData = JSONObject()
                jsonData.put("isConnected", isConnected)
                jsonData.put("temperature", "0")
                SharedStreamHandler.getInstance().sendEvent(jsonData)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeviceData)
            .observeForever  {
                val data = it.data as DeviceInfo
                // data_log.text = "$data"
                // data.battery：1-10（1：10%，2：20%...8：80%，9：90%，10：100%）
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg)
            .observeForever  {
                val data = it.data as ErrorResult
                // data_log.text = "$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData)
            .observeForever  {

                val data = it.data as TempResult
//

                Log.d("temp", "${data.temp}")

                if (data.temp>0) {
                    BleServiceHelper.BleServiceHelper.stopScan()
                    BleServiceHelper.BleServiceHelper.disconnect(false)
                    isConnected=false

                }

               // result.success(data.temp)

//                sendEvent("${data.temp}")
                val jsonData = JSONObject()
                jsonData.put("isConnected", isConnected)
                jsonData.put("temperature", data.temp.toString())
                SharedStreamHandler.getInstance().sendEvent(jsonData)


            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempList)
            .observeForever {
                val data = it.data as ArrayList<Record>
                //  data_log.text = data.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeleteData)
            .observeForever  {
                val data = it.data as Boolean
                // data_log.text = "DeleteData $data"
            }

    }



    private fun checkBt() {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val adapter: BluetoothAdapter? = bluetoothManager?.adapter

        if (adapter == null) {
            Toast.makeText(context, "Bluetooth is not supported on this device.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!adapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Check BLUETOOTH_CONNECT permission for Android 12+
                if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Bluetooth is not enabled. Please enable it.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Bluetooth permission is required.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Bluetooth is not enabled. Please enable it.", Toast.LENGTH_SHORT).show()
            }
        } else {
            needService()
        }
    }





    override fun onBleStateChanged(model: Int, state: Int) {
        // 蓝牙状态 Ble.State
        Log.d(TAG, "model $model, state: $state")

     //   _bleState.value = state == Ble.State.CONNECTED
    }

    private fun initService() {

        val application = context.applicationContext as? Application
            ?: throw IllegalStateException("Context is not an instance of Application")
        if (BleServiceHelper.BleServiceHelper.checkService()) {
            // BleService already init
            BleServiceHelper.BleServiceHelper.startScan(intArrayOf(model))
        } else {

            BleServiceHelper.BleServiceHelper.initLog(true).initService(application)
        }
    }

    private fun needService() {
        var gpsEnabled = false
        var networkEnabled = false
        val lm = context. getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            Log.d("needService"," ******************   gpsEnabled:${gpsEnabled}   networkEnabled: ${networkEnabled}  ");

        } catch (ex: Exception) {

            Log.d("needService"," ******************   printStackTrace   ");

            ex.printStackTrace()
        }
        if (!gpsEnabled && !networkEnabled) {
            Log.d("needService"," ******************   open location service ");

          //  Log.d("location"," ******************    open location service");
//            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
//            dialog.setMessage("open location service")
//            dialog.setPositiveButton("ok") { _, _ ->
//                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivityForResult(myIntent, 888)
//            }
//            dialog.setNegativeButton("cancel") { _, _ ->
//                finish()
//            }
//            dialog.setCancelable(false)
//            dialog.show()

        } else {
            Log.d("needService"," ******************   initService ");

            initService()
        }
    }








}