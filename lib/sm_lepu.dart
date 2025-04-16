

import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:sm_lepu/event_data.dart';
import 'package:sm_lepu/reading_type.dart';

class SmLepu {
  final MethodChannel methodChannel = const MethodChannel('sm_lepu');
  final EventChannel _eventChannel = const EventChannel('sm_lepu_events');
  ReadingType readingType=ReadingType.temp;

  Future checkPermission() async {
    await methodChannel.invokeMethod('checkPermission');
  }
  Future<bool> isPermissionsGranted() async {
   return await methodChannel.invokeMethod('isPermissionsGranted');
  }
  Future<dynamic> readTemp() async {
    readingType=ReadingType.temp;
    return  await methodChannel.invokeMethod('readTemp');
  }
  Future<dynamic> readSpo2() async {
    readingType=ReadingType.spo2;

    return  await methodChannel.invokeMethod('readSpo2');
  }

  Future<dynamic> initBP() async {
    readingType=ReadingType.bloodPressure;

    return  await methodChannel.invokeMethod('initBP');
  }
  Future<dynamic> startBP() async {
    readingType=ReadingType.bloodPressure;

    return  await methodChannel.invokeMethod('startBP');
  }

  Future<dynamic> stopBP() async {
    return  await methodChannel.invokeMethod('stopBP');
  }


  Future<dynamic> dispose() async {
       methodChannel.invokeMethod('dispose');
  }


  Stream<EventData> getEvents() {
   return _eventChannel.receiveBroadcastStream().map((event) {
      // Assuming the event is a JSON string, parse it
      var decodedJson = jsonDecode(event);

      // Convert the decoded JSON to EventData model
      EventData eventData = EventData.fromJson(decodedJson);

      // Return the EventData object
      return eventData;
    });    // return _eventChannel.receiveBroadcastStream().map((event) => event.toString());
  }

  startMeasure({required ReadingType type})async{

    switch(type){

      case ReadingType.bloodPressure:{
        initBP();
      }
      case ReadingType.spo2:{
        readSpo2();
      }
      case ReadingType.temp:{
        readTemp();
      }
    }



  }


}
