import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:sm_lepu/lepu_event_data.dart';

// Export the unified model
export 'package:sm_lepu/lepu_event_data.dart';

/// Flutter plugin for Lepu Medical Bluetooth health devices
/// Supports temperature, SpO2, blood pressure, and weight measurements
class SmLepu {
  final MethodChannel _methodChannel = const MethodChannel('sm_lepu');
  final EventChannel _eventChannel = const EventChannel('sm_lepu_events');

  // ============================================
  // Permission Methods
  // ============================================

  /// Request Bluetooth permissions
  Future<void> checkPermission() async {
    await _methodChannel.invokeMethod('checkPermission');
  }

  /// Check if Bluetooth permissions are granted
  Future<bool> isPermissionsGranted() async {
    return await _methodChannel.invokeMethod('isPermissionsGranted');
  }

  // ============================================
  // Temperature (AOJ-20A)
  // ============================================

  /// Start temperature reading from AOJ-20A thermometer
  Future<bool> readTemp() async {
    return await _methodChannel.invokeMethod('readTemp');
  }

  // ============================================
  // SpO2 (PC60FW)
  // ============================================

  /// Start SpO2 and heart rate reading from PC60FW oximeter
  Future<bool> readSpo2() async {
    return await _methodChannel.invokeMethod('readSpo2');
  }

  // ============================================
  // Blood Pressure (PC-102)
  // ============================================

  /// Initialize blood pressure monitor PC-102
  Future<bool> initBP() async {
    return await _methodChannel.invokeMethod('initBP');
  }

  /// Start blood pressure measurement
  Future<bool> startBP() async {
    return await _methodChannel.invokeMethod('startBP');
  }

  /// Stop blood pressure measurement
  Future<bool> stopBP() async {
    return await _methodChannel.invokeMethod('stopBP');
  }

  // ============================================
  // Weight Scale (ICOMON)
  // ============================================

  /// Initialize the ICOMON weight scale SDK and start scanning
  Future<bool> initWeight() async {
    return await _methodChannel.invokeMethod('initWeight');
  }

  /// Start scanning for weight scale devices
  Future<bool> scanWeight() async {
    return await _methodChannel.invokeMethod('scanWeight');
  }

  /// Stop scanning for weight scale devices
  Future<bool> stopWeightScan() async {
    return await _methodChannel.invokeMethod('stopWeightScan');
  }

  /// Dispose weight scale resources
  Future<bool> disposeWeight() async {
    return await _methodChannel.invokeMethod('disposeWeight');
  }

  // ============================================
  // Cleanup
  // ============================================

  /// Dispose all device connections and resources
  Future<void> dispose() async {
    await _methodChannel.invokeMethod('dispose');
  }

  // ============================================
  // Unified Event Stream
  // ============================================

  /// Get unified event stream for ALL devices (temperature, SpO2, BP, weight)
  /// Returns [LepuEventData] with deviceType indicating the source device
  Stream<LepuEventData> getEvents() {
    return _eventChannel.receiveBroadcastStream().map((event) {
      return LepuEventData.fromJson(event);
    });
  }
}
