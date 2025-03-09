

import 'package:flutter/services.dart';

class SmLepu {
  final MethodChannel methodChannel = const MethodChannel('sm_lepu');
  final EventChannel _eventChannel = const EventChannel('sm_lepu_events');


  Future checkPermission() async {
    await methodChannel.invokeMethod('checkPermission');
  }
  Future<bool> isPermissionsGranted() async {
   return await methodChannel.invokeMethod('isPermissionsGranted');
  }
  Future<dynamic> readTemp() async {
    return  await methodChannel.invokeMethod('readTemp');
  }
  Future<dynamic> readSpo2() async {
    return  await methodChannel.invokeMethod('readSpo2');
  }

  Future<dynamic> startBP() async {
    return  await methodChannel.invokeMethod('startBP');
  }

  Future<dynamic> stopBP() async {
    return  await methodChannel.invokeMethod('stopBP');
  }


  Future<dynamic> dispose() async {
       methodChannel.invokeMethod('dispose');
  }


  Stream<String> getEvents() {
    return _eventChannel.receiveBroadcastStream().map((event) => event.toString());
  }

}
