package com.sm.sm_lepu

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.observer.BleChangeObserver

abstract class BaseDeviceHelper : BleChangeObserver {

    lateinit var context: Context
    lateinit var activity: Activity

    // Abstract property for the model, to be overridden by subclasses
    protected abstract val model: Int
    protected abstract val tagName: String
    protected abstract val deviceTypeString: String
    
    // Default timeout 15 seconds
    protected var scanTimeout: Long = 15000 
    private val handler = Handler(Looper.getMainLooper())
    private val stopScanRunnable = Runnable { 
        Log.d(tagName, "Scan timeout reached, stopping scan")
        BleServiceHelper.BleServiceHelper.stopScan() 
        
        // Notify Flutter about the timeout error
        val jsonData = JSONObject()
        jsonData.put("deviceType", deviceTypeString)
        jsonData.put("hasError", true)
        jsonData.put("message", "Scan timeout reached")
        SharedStreamHandler.getInstance().sendEvent(jsonData)
    }


    fun initialize(activity: Activity, context: Context) {
        this.context = context
        this.activity = activity
    }

    open fun init() {
        // Only start checking Bluetooth, do not init events yet
        checkBt()
    }

    // Abstract method for subclasses to register their specific LiveEventBus observers
    // This will be called only after Location and Bluetooth checks pass
    protected abstract fun initEvents()

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

    private fun needService() {
        var gpsEnabled = false
        var networkEnabled = false
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            Log.d(tagName, " ******************   gpsEnabled:${gpsEnabled}   networkEnabled: ${networkEnabled}  ")

        } catch (ex: Exception) {
            Log.d(tagName, " ******************   printStackTrace   ")
            ex.printStackTrace()
        }
        if (!gpsEnabled && !networkEnabled) {
            Log.d(tagName, " ******************   open location service ")

            val dialog: AlertDialog.Builder = AlertDialog.Builder(activity)
            dialog.setTitle("Location Services Required")
            dialog.setMessage("To scan for Bluetooth devices, Location Services must be enabled. Please enable Location Services in Settings.")
            dialog.setPositiveButton("Go to Settings") { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                // Using 888 as request code, same as before
                activity.startActivityForResult(myIntent, 888)
            }
            dialog.setNegativeButton("Cancel") { _, _ ->
                activity.finish()
            }
            dialog.setCancelable(false)
            dialog.show()

        } else {
            Log.d(tagName, " ******************   initService ")
            initService()
        }
    }

    private fun initService() {
        val application = context.applicationContext as? Application
            ?: throw IllegalStateException("Context is not an instance of Application")

        // Initialize events NOW, because we are about to start the service logic
        initEvents()

        Log.d(tagName, "Starting scan with timeout: ${scanTimeout}ms")
        // Schedule timeout
        handler.removeCallbacks(stopScanRunnable)
        handler.postDelayed(stopScanRunnable, scanTimeout)

        if (BleServiceHelper.BleServiceHelper.checkService()) {
            // BleService already init, start scan immediately
            BleServiceHelper.BleServiceHelper.startScan(intArrayOf(model))
        } else {
            // Init service, which will trigger EventServiceConnectedAndInterfaceInit later
            BleServiceHelper.BleServiceHelper.initLog(true).initService(application)
        }
    }

    protected fun cancelScanTimeout() {
        Log.d(tagName, "Cancelling scan timeout")
        handler.removeCallbacks(stopScanRunnable)
    }

    open fun dispose() {
        handler.removeCallbacks(stopScanRunnable)
        BleServiceHelper.BleServiceHelper.stopScan()
        BleServiceHelper.BleServiceHelper.disconnect(false)
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        Log.d(tagName, "model $model, state: $state")
    }
}
