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
          child: Column(
            children: [

              Text("result \n$result"),


              TextButton(onPressed: (){

                _smLepuPlugin.checkPermission();

              }, child: Text("Check Permission")),
              TextButton(onPressed: ()async{

                _smLepuPlugin.getEvents().listen((onData){


                  result=onData;
                  setState(() {

                  });

                });

                await  _smLepuPlugin.readTemp();





              }, child: Text("read temp")),

              TextButton(onPressed: ()async{

                _smLepuPlugin.getEvents().listen((onData){


                  result=onData;
                  setState(() {

                  });

                });

                await  _smLepuPlugin.readSpo2();





              }, child: Text("read Spo2")),

              // Text('Running on: $_platformVersion\n'),
            ],
          ),
        ),
      ),
    );
  }
}
