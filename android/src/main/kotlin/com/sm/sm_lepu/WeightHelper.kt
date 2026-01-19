package com.sm.sm_lepu

import android.content.Context
import android.util.Log
import cn.icomon.icdevicemanager.ICDeviceManager
import cn.icomon.icdevicemanager.ICDeviceManagerDelegate
import cn.icomon.icdevicemanager.callback.ICScanDeviceDelegate
import cn.icomon.icdevicemanager.model.data.*
import cn.icomon.icdevicemanager.model.device.ICDevice
import cn.icomon.icdevicemanager.model.device.ICDeviceInfo
import cn.icomon.icdevicemanager.model.device.ICScanDeviceInfo
import cn.icomon.icdevicemanager.model.device.ICUserInfo
import cn.icomon.icdevicemanager.model.other.ICConstant
import cn.icomon.icdevicemanager.model.other.ICDeviceManagerConfig
import io.flutter.plugin.common.EventChannel
import org.json.JSONObject
import java.util.Locale

class WeightHelper(private val applicationContext: Context) :
    ICScanDeviceDelegate, ICDeviceManagerDelegate {

    companion object {
        private const val TAG = "WeightHelper"
    }

    private var deviceInfo: ICScanDeviceInfo? = null
    private var device: ICDevice? = null
    private var connectionStateName = ""
    private var currentWeight = 0.0
    private val discoveredDevices = ArrayList<ICScanDeviceInfo>()
    private var sdkInitialized = false

    // User info defaults
    private val height = 170
    private val age = 24
    private val sex = 1

    fun init() {
        reset()
        if (sdkInitialized) {
            // SDK already initialized, go straight to scan
            Log.d(TAG, "SDK already initialized, starting scan directly")
            scan()
        } else {
            initSDK()
        }
    }

    private fun reset() {
        device = null
        deviceInfo = null
        connectionStateName = ""
        currentWeight = 0.0
        discoveredDevices.clear()
        Log.d(TAG, "State reset for new measurement")
    }

    private fun initSDK() {
        val config = ICDeviceManagerConfig()
        config.context = applicationContext

        val userInfo = ICUserInfo().apply {
            this.age = this@WeightHelper.age
            this.height = this@WeightHelper.height
            this.sex = ICConstant.ICSexType.ICSexTypeMale
            this.peopleType = ICConstant.ICPeopleType.ICPeopleTypeNormal
        }

        ICDeviceManager.shared().setDelegate(this)
        ICDeviceManager.shared().updateUserInfo(userInfo)
        ICDeviceManager.shared().initMgrWithConfig(config)
    }

    fun scan() {
        discoveredDevices.clear()
        ICDeviceManager.shared().scanDevice(this)
        Log.d(TAG, "Started scanning for weight scales")
    }

    fun stopScan() {
        try {
            ICDeviceManager.shared().stopScan()
            Log.d(TAG, "Scan stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan: ${e.message}")
        }
    }

    fun dispose() {
        try {
            stopScan()
            
            // Send disconnected event before cleanup
            connectionStateName = "Disconnected"
            try {
                val jsonData = buildEvent(
                    isConnected = false,
                    message = "Disposed"
                )
                sendEvent(jsonData)
                Log.d(TAG, "Sent disconnected event")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending disconnect event: ${e.message}")
            }
            
            device?.let { dev ->
                ICDeviceManager.shared().removeDevice(dev) { _, code ->
                    Log.d(TAG, "Device removed: $code")
                }
            }
            device = null
            deviceInfo = null
            currentWeight = 0.0
            discoveredDevices.clear()
            Log.d(TAG, "Disposed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing: ${e.message}")
        }
    }

    private fun sendEvent(jsonData: JSONObject) {
        SharedStreamHandler.getInstance().sendEvent(jsonData)
    }

    /// Create standardized event JSON with consistent keys for all device types
    private fun buildEvent(
        isConnected: Boolean = false,
        isCompleted: Boolean = false,
        hasError: Boolean = false,
        message: String = "",
        progress: Int = 0,
        weightValue: Double = currentWeight
    ): JSONObject {
        return JSONObject().apply {
            put("deviceType", "weight")
            put("isConnected", isConnected)
            put("isCompleted", isCompleted)
            put("hasError", hasError)
            put("message", message)
            put("progress", progress)
            put("weight", weightValue)
            put("temperature", 0.0)
            put("spo2", 0)
            put("heart_rate", 0)
            put("systolic", 0)
            put("diastolic", 0)
        }
    }

    private fun sendConnectionState() {
        try {
            val jsonData = buildEvent(
                isConnected = connectionStateName == "Connected",
                message = connectionStateName
            )
            sendEvent(jsonData)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending connection state: ${e.message}")
        }
    }

    private fun addLog(log: String) {
        try {
            val jsonData = buildEvent(
                isConnected = connectionStateName == "Connected",
                message = log
            )
            Log.d(TAG, log)
            sendEvent(jsonData)
        } catch (e: Exception) {
            Log.e(TAG, "Error in addLog: ${e.message}")
        }
    }

    // Connect to discovered device
    private fun connectToDevice(deviceInfo: ICScanDeviceInfo) {
        if (device == null) {
            device = ICDevice()
        }
        device?.macAddr = deviceInfo.macAddr

        ICDeviceManager.shared().addDevice(device) { dev, code ->
            connectionStateName = when (code.ordinal) {
                0 -> "Connected"
                1 -> "Disconnected"
                else -> "Unknown state"
            }
            addLog("Device state: ${code.name}")
        }

        Log.d(TAG, "Connecting to device: ${deviceInfo.name} - ${deviceInfo.macAddr}")
    }

    // ICScanDeviceDelegate - Called when a device is found during scan
    override fun onScanResult(deviceInfo: ICScanDeviceInfo?) {
        deviceInfo?.let { info ->
            // Check if device already in list
            val existingDevice = discoveredDevices.find { 
                it.macAddr.equals(info.macAddr, ignoreCase = true) 
            }
            
            if (existingDevice != null) {
                existingDevice.rssi = info.rssi
            } else {
                discoveredDevices.add(info)
                Log.d(TAG, "Discovered device: ${info.name} - ${info.macAddr}")
            }

            // Auto-connect to first compatible scale found
            // You can filter by device name here if needed
            // Original filter was: deviceInfo1.getName().toLowerCase().contains("my_scale")
            if (device == null) {
                ICDeviceManager.shared().stopScan()
                connectToDevice(info)
            }
        }
    }

    // ICDeviceManagerDelegate implementations
    override fun onInitFinish(success: Boolean) {
        addLog("SDK init result: $success")
        if (success) {
            sdkInitialized = true
            scan()
        }
    }

    override fun onBleState(state: ICConstant.ICBleState?) {
        state?.let {
            addLog("BLE state: ${it.name}")
            if (it == ICConstant.ICBleState.ICBleStatePoweredOff) {
                connectionStateName = "PoweredOff"
            }
        }
    }

    override fun onDeviceConnectionChanged(device: ICDevice?, state: ICConstant.ICDeviceConnectState?) {
        val stateName = when (state?.ordinal) {
            0 -> "Connected"
            1 -> "Disconnected"
            else -> "Unknown state"
        }
        connectionStateName = stateName
        
        // Send proper connection event to Flutter
        try {
            val jsonData = buildEvent(
                isConnected = stateName == "Connected",
                message = stateName
            )
            sendEvent(jsonData)
            Log.d(TAG, "Connection state sent: $stateName")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending connection state: ${e.message}")
        }
    }

    override fun onReceiveWeightData(device: ICDevice?, data: ICWeightData?) {
        data?.let {
            currentWeight = it.weight_kg

            if (it.isStabilized && it.imp != 0.0) {
                try {
                    // Send interim weight data
                    val jsonData = buildEvent(
                        isConnected = true,
                        weightValue = it.weight_kg
                    )
                    sendEvent(jsonData)

                    // Remove device after successful reading
                    this.device?.let { dev ->
                        ICDeviceManager.shared().removeDevice(dev) { _, code ->
                            connectionStateName = "Complete Reading"
                            // Reset device for new measurements
                            this@WeightHelper.device = null
                            this@WeightHelper.deviceInfo = null
                            try {
                                val finalJson = buildEvent(
                                    isConnected = false,
                                    isCompleted = true,
                                    message = "Measurement complete",
                                    weightValue = it.weight_kg
                                )
                                sendEvent(finalJson)
                                Log.d(TAG, "Measurement complete. Device reset for next use.")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error sending final data: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing weight data: ${e.message}")
                }
            } else if (!it.isStabilized) {
                currentWeight = it.weight_kg
                addLog(String.format(Locale.ENGLISH, "weight= %.2f", it.weight_kg))
            }
        }
    }

    override fun onReceiveMeasureStepData(device: ICDevice?, step: ICConstant.ICMeasureStep?, data: Any?) {
        when (step) {
            ICConstant.ICMeasureStep.ICMeasureStepMeasureWeightData -> {
                (data as? ICWeightData)?.let { onReceiveWeightData(device, it) }
            }
            ICConstant.ICMeasureStep.ICMeasureStepMeasureOver -> {
                (data as? ICWeightData)?.let {
                    it.isStabilized = true
                    addLog("Measure over")
                    onReceiveWeightData(device, it)
                }
            }
            ICConstant.ICMeasureStep.ICMeasureStepAdcStart -> addLog("Start imp...")
            ICConstant.ICMeasureStep.ICMeasureStepAdcResult -> addLog("Imp over")
            ICConstant.ICMeasureStep.ICMeasureStepHrStart -> addLog("Start hr")
            ICConstant.ICMeasureStep.ICMeasureStepHrResult -> {
                (data as? ICWeightData)?.let { addLog("Over hr: ${it.hr}") }
            }
            else -> {}
        }
    }

    // Unused callbacks - required by interface
    override fun onNodeConnectionChanged(device: ICDevice?, nodeId: Int, state: ICConstant.ICDeviceConnectState?) {}
    override fun onReceiveKitchenScaleData(device: ICDevice?, data: ICKitchenScaleData?) {}
    override fun onReceiveKitchenScaleUnitChanged(device: ICDevice?, unit: ICConstant.ICKitchenScaleUnit?) {}
    override fun onReceiveCoordData(device: ICDevice?, data: ICCoordData?) {}
    override fun onReceiveRulerData(device: ICDevice?, data: ICRulerData?) {}
    override fun onReceiveRulerHistoryData(device: ICDevice?, data: ICRulerData?) {}
    override fun onReceiveWeightCenterData(device: ICDevice?, data: ICWeightCenterData?) {}
    override fun onReceiveWeightUnitChanged(device: ICDevice?, unit: ICConstant.ICWeightUnit?) {}
    override fun onReceiveRulerUnitChanged(device: ICDevice?, unit: ICConstant.ICRulerUnit?) {}
    override fun onReceiveRulerMeasureModeChanged(device: ICDevice?, mode: ICConstant.ICRulerMeasureMode?) {}
    override fun onReceiveWeightHistoryData(device: ICDevice?, data: ICWeightHistoryData?) {}
    override fun onReceiveSkipData(device: ICDevice?, data: ICSkipData?) {}
    override fun onReceiveHistorySkipData(device: ICDevice?, data: ICSkipData?) {}
    override fun onReceiveBattery(device: ICDevice?, battery: Int, ext: Any?) {}
    override fun onReceiveUpgradePercent(device: ICDevice?, status: ICConstant.ICUpgradeStatus?, percent: Int) {}
    override fun onReceiveDeviceInfo(device: ICDevice?, info: ICDeviceInfo?) {}
    override fun onReceiveDebugData(device: ICDevice?, type: Int, data: Any?) {}
    override fun onReceiveConfigWifiResult(device: ICDevice?, state: ICConstant.ICConfigWifiState?) {}
    override fun onReceiveHR(device: ICDevice?, hr: Int) {}
    override fun onReceiveUserInfo(device: ICDevice?, userInfo: ICUserInfo?) {}
    override fun onReceiveRSSI(device: ICDevice?, rssi: Int) {}
}
