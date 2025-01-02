

import 'package:flutter/services.dart';

class SmLepu {
  final MethodChannel methodChannel = const MethodChannel('sm_lepu');
  final EventChannel _eventChannel = const EventChannel('sm_lepu_events');


  Future checkPermission() async {
    await methodChannel.invokeMethod('checkPermission');
  }
  Future<dynamic> readTemp() async {
    return  await methodChannel.invokeMethod('readTemp');
  }
  Future<dynamic> readSpo2() async {
    return  await methodChannel.invokeMethod('readSpo2');
  }


  Stream<String> getEvents() {
    return _eventChannel.receiveBroadcastStream().map((event) => event.toString());
  }

}
