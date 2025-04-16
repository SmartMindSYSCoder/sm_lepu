import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:sm_lepu/sm_lepu.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _smLepuPlugin = SmLepu();
  String responseHistory='';



  String result="";

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: SingleChildScrollView(
            child: Column(
              children: [

                Text("result \n$result"),


                TextButton(onPressed: (){

                  _smLepuPlugin.checkPermission();

                }, child: Text("Check Permission")),
                TextButton(onPressed: (){

                  _smLepuPlugin.dispose();

                }, child: Text("Dispose")),
                TextButton(onPressed: ()async{

                  _smLepuPlugin.getEvents().listen((event){

                    result= ""
                        "Connected :${event.isConnected}\n"
                        "Completed :${event.isCompleted}\n"
                        "Temperature :${event.temperature}\n"
                        "";
                    responseHistory+='\n\n${event.toJson()}\n';
                    setState(() {

                    });

                  });

                  await  _smLepuPlugin.readTemp();





                }, child: Text("read temp")),

                TextButton(onPressed: ()async{

                  _smLepuPlugin.getEvents().listen((event){


                    result= ""
                        "Connected :${event.isConnected}\n"
                        "Completed :${event.isCompleted}\n"
                        "Heart Rate :${event.heartRate}\n"
                        "Spo2 :${event.spo2}\n"
                        "";
                    responseHistory+='\n\n${event.toJson()}\n';

                    setState(() {

                    });

                  });

                  await  _smLepuPlugin.readSpo2();





                }, child: Text("read Spo2")),
                TextButton(onPressed: ()async{

                  _smLepuPlugin.getEvents().listen((event){


                    result= ""
                        "Connected :${event.isConnected}\n"
                        "Completed :${event.isCompleted}\n"
                        "Progress :${event.progress}\n"
                        "systolic :${event.systolic}\n"
                        "diastolic :${event.diastolic}\n"
                        "";
                    setState(() {

                    });

                  });

                  await  _smLepuPlugin.initBP();





                }, child: Text("start Blood Pressure")),


                Text("Response History \n$responseHistory"),


                // Text('Running on: $_platformVersion\n'),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
