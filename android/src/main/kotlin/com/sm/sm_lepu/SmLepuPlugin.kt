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

/** SmLepuPlugin */
class SmLepuPlugin: FlutterPlugin, MethodCallHandler,ActivityAware {
  private lateinit var channel: MethodChannel
  private lateinit var lepuEventChannel: EventChannel

  private var applicationContext: Context? = null
  private var activity: Activity? = null

  private var permissionHelper: PermissionHelper? = null
  private val aoj20aHelper = Aoj20aHelper()
  private val pc60fwHelper = Pc60fwHelper()

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "sm_lepu")
    lepuEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "sm_lepu_events")

    channel.setMethodCallHandler(this)

    lepuEventChannel.setStreamHandler(SharedStreamHandler.getInstance()) // Attach EventManager


    applicationContext = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "checkPermission" -> {
        permissionHelper?.checkPermissions()
      }

      "readTemp" -> {

        if(permissionHelper?.isPermissionsGranted()==true){
          aoj20aHelper?.init()
        }
        else{
          permissionHelper?.checkPermissions()
        }


      }

      "readSpo2" -> {


        if(permissionHelper?.isPermissionsGranted()==true){
          pc60fwHelper?.init()
        }
        else{
          permissionHelper?.checkPermissions()
        }


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
}