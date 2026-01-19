# SM Lepu

Flutter plugin for Lepu Medical Bluetooth health devices.

## Supported Devices

| Device | Measurement |
|--------|-------------|
| **AOJ-20A** | Temperature |
| **PC60FW** | SpO2 & Heart Rate |
| **PC-102** | Blood Pressure |
| **ICOMON Scale** | Weight |

## Installation

```yaml
dependencies:
  sm_lepu:
    git: https://github.com/SmartMindSYSCoder/sm_lepu.git
```

## Quick Start

```dart
import 'package:sm_lepu/sm_lepu.dart';

final smLepu = SmLepu();
await smLepu.checkPermission();
```

## Unified Event Model

**Single event stream for ALL devices:**

```dart
smLepu.getEvents().listen((e) {
  switch (e.deviceType) {
    case LepuDeviceType.temperature:
      print('Temp: ${e.temperature}°C');
    case LepuDeviceType.spo2:
      print('SpO2: ${e.spo2}%, HR: ${e.heartRate}');
    case LepuDeviceType.bloodPressure:
      print('BP: ${e.systolic}/${e.diastolic}');
    case LepuDeviceType.weight:
      print('Weight: ${e.weight} kg');
  }
});
```

**LepuEventData fields:**
- `deviceType`: Identifies device (temperature, spo2, bloodPressure, weight). Available immediately upon connection.
- `state`: disconnected, connecting, connected, measuring, completed, error
- `isConnected`, `isCompleted`, `hasError`, `message`, `progress`
- `weight`: Formatted to 2 decimal places (e.g., 75.50)
- `temperature`, `spo2`, `heartRate`, `systolic`, `diastolic`

## Device Methods

```dart
// Temperature
await smLepu.readTemp();

// SpO2
await smLepu.readSpo2();

// Blood Pressure
await smLepu.initBP();
await smLepu.startBP();
await smLepu.stopBP();

// Weight
await smLepu.initWeight();
await smLepu.stopWeightScan();
await smLepu.disposeWeight();

// Cleanup
await smLepu.dispose();
```

## Android Permissions

```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

## Requirements

Flutter >=3.3.0 | Dart ^3.5.0 | Android minSdk 24

## Author

[SmartMind SYS](https://github.com/SmartMindSYSCoder)
