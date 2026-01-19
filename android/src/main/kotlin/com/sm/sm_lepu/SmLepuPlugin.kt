package com.sm.sm_lepu


import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel
import com.lepu.blepro.ext.BleServiceHelper

/** SmLepuPlugin */
class SmLepuPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel: MethodChannel
  private lateinit var lepuEventChannel: EventChannel

  private var applicationContext: Context? = null
  private var activity: Activity? = null

  private var permissionHelper: PermissionHelper? = null
  private val aoj20aHelper = Aoj20aHelper()
  private val pc60fwHelper = Pc60fwHelper()
  private val pc102Helper = PC102Helper()
  private var weightHelper: WeightHelper? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "sm_lepu")
    lepuEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "sm_lepu_events")

    channel.setMethodCallHandler(this)
    lepuEventChannel.setStreamHandler(SharedStreamHandler.getInstance())

    applicationContext = flutterPluginBinding.applicationContext
    applicationContext?.let {
      weightHelper = WeightHelper(it)
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "checkPermission" -> {
        permissionHelper?.checkPermissions()
        result.success(true)
      }

      "isPermissionsGranted" -> {
        result.success(permissionHelper?.isPermissionsGranted() ?: false)
      }

      "readTemp" -> {
        if (permissionHelper?.isPermissionsGranted() == true) {
          aoj20aHelper.init()
          result.success(true)
        } else {
          permissionHelper?.checkPermissions()
          result.success(false)
        }
      }

      "readSpo2" -> {
        if (permissionHelper?.isPermissionsGranted() == true) {
          pc60fwHelper.init()
          result.success(true)
        } else {
          permissionHelper?.checkPermissions()
          result.success(false)
        }
      }

      "initBP" -> {
        if (permissionHelper?.isPermissionsGranted() == true) {
          pc102Helper.init()
          result.success(true)
        } else {
          permissionHelper?.checkPermissions()
          result.success(false)
        }
      }

      "startBP" -> {
        if (permissionHelper?.isPermissionsGranted() == true) {
          pc102Helper.startBP()
          result.success(true)
        } else {
          permissionHelper?.checkPermissions()
          result.success(false)
        }
      }

      "stopBP" -> {
        if (permissionHelper?.isPermissionsGranted() == true) {
          pc102Helper.stopBP()
          result.success(true)
        } else {
          permissionHelper?.checkPermissions()
          result.success(false)
        }
      }

      "dispose" -> {
        dispose()
        result.success(true)
      }

      // Weight Scale Methods
      "initWeight" -> {
        if (permissionHelper?.isPermissionsGranted() == true) {
          weightHelper?.init()
          result.success(true)
        } else {
          permissionHelper?.checkPermissions()
          result.success(false)
        }
      }

      "scanWeight" -> {
        if (permissionHelper?.isPermissionsGranted() == true) {
          weightHelper?.scan()
          result.success(true)
        } else {
          permissionHelper?.checkPermissions()
          result.success(false)
        }
      }

      "stopWeightScan" -> {
        weightHelper?.stopScan()
        result.success(true)
      }

      "disposeWeight" -> {
        weightHelper?.dispose()
        result.success(true)
      }

      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    lepuEventChannel.setStreamHandler(null)
  }

  override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
    activity = activityPluginBinding.activity
    applicationContext?.let {
      permissionHelper = PermissionHelper(activity!!, it)
      aoj20aHelper.initialize(activity!!, it)
      pc60fwHelper.initialize(activity!!, it)
      pc102Helper.initialize(activity!!, it)
    }
  }

  override fun onDetachedFromActivityForConfigChanges() {
    // This call will be followed by onReattachedToActivityForConfigChanges().
  }

  override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
    activity = activityPluginBinding.activity
  }

  override fun onDetachedFromActivity() {
    activity = null
    permissionHelper = null
  }

  private fun dispose() {
    BleServiceHelper.BleServiceHelper.stopScan()
    BleServiceHelper.BleServiceHelper.disconnect(false)
    weightHelper?.dispose()
  }
}

