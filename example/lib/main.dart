import 'package:flutter/material.dart';
import 'dart:async';
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
  final _smLepu = SmLepu();
  String result = "";
  StreamSubscription? _eventSub;

  @override
  void dispose() {
    _eventSub?.cancel();
    _smLepu.dispose();
    super.dispose();
  }

  void _subscribeToEvents() {
    _eventSub?.cancel();
    _eventSub = _smLepu.getEvents().listen((e) {
      setState(() {
        // Display based on device type
        switch (e.deviceType) {
          case LepuDeviceType.temperature:
            result = "📌 Temperature\n"
                "State: ${e.state.name}\n"
                "Temp: ${e.temperature}°C";
            break;
          case LepuDeviceType.spo2:
            result = "📌 SpO2\n"
                "State: ${e.state.name}\n"
                "SpO2: ${e.spo2}%\n"
                "HR: ${e.heartRate} BPM";
            break;
          case LepuDeviceType.bloodPressure:
            result = "📌 Blood Pressure\n"
                "State: ${e.state.name}\n"
                "Progress: ${e.progress}%\n"
                "BP: ${e.systolic}/${e.diastolic} mmHg";
            break;
          case LepuDeviceType.weight:
            result = "📌 Weight\n"
                "State: ${e.state.name}\n"
                "Connected: ${e.isConnected}\n"
                "Weight: ${e.weight} kg\n"
                "Completed: ${e.isCompleted}"
                "${e.message.isNotEmpty ? '\nMsg: ${e.message}' : ''}";
            break;
          case LepuDeviceType.unknown:
            result = "📌 Unknown Device\nState: ${e.state.name}";
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('SM Lepu Plugin'),
          backgroundColor: Colors.teal,
          foregroundColor: Colors.white,
        ),
        body: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Permissions
              _section("Permissions", [
                ElevatedButton(
                  onPressed: () => _smLepu.checkPermission(),
                  child: const Text("Request Permissions"),
                ),
              ]),

              if (result.isNotEmpty) _resultCard("Event Data", result),

              // Vital Signs
              _section("Vital Signs", [
                _deviceButton(Icons.thermostat, "Temperature", () {
                  _subscribeToEvents();
                  _smLepu.readTemp();
                }),
                _deviceButton(Icons.favorite, "SpO2", () {
                  _subscribeToEvents();
                  _smLepu.readSpo2();
                }),
                _deviceButton(Icons.monitor_heart, "Blood Pressure", () {
                  _subscribeToEvents();
                  _smLepu.initBP();
                }),
              ]),

              // Weight Scale
              _section("Weight Scale", [
                _deviceButton(Icons.scale, "Start Weight", () async {
                  setState(() => result = "📌 Weight\nState: Initializing...");
                  _subscribeToEvents();
                  await Future.delayed(const Duration(milliseconds: 100));
                  _smLepu.initWeight();
                }),
                const SizedBox(height: 8),
                Row(children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => _smLepu.stopWeightScan(),
                      child: const Text("Stop Scan"),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => _smLepu.disposeWeight(),
                      child: const Text("Dispose"),
                    ),
                  ),
                ]),
              ]),

              // Cleanup
              _section("Cleanup", [
                OutlinedButton(
                  onPressed: () {
                    _eventSub?.cancel();
                    _smLepu.dispose();
                    setState(() => result = "");
                  },
                  child: const Text("Dispose All"),
                ),
              ]),
            ],
          ),
        ),
      ),
    );
  }

  Widget _section(String title, List<Widget> children) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(title,
                style:
                    const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _deviceButton(IconData icon, String label, VoidCallback onPressed) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: ElevatedButton.icon(
        onPressed: onPressed,
        icon: Icon(icon),
        label: Text(label),
      ),
    );
  }

  Widget _resultCard(String title, String content) {
    return Card(
      color: Colors.grey[100],
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
            const Divider(),
            Text(content,
                style: const TextStyle(fontFamily: 'monospace', fontSize: 13)),
          ],
        ),
      ),
    );
  }
}
