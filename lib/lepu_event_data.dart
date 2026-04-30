import 'dart:convert';

/// Device type enum to identify which device sent the event
enum LepuDeviceType {
  temperature,
  spo2,
  bloodPressure,
  weight,
  unknown,
}

/// Connection state enum for device status
enum LepuConnectionState {
  disconnected,
  connecting,
  connected,
  measuring,
  completed,
  error,
}

/// Unified event data model for all Lepu devices
/// Handles temperature, SpO2, blood pressure, and weight measurements
class LepuEventData {
  /// The type of device that generated this event
  final LepuDeviceType deviceType;

  /// Current connection state
  final LepuConnectionState state;

  /// Whether the device is currently connected
  final bool isConnected;

  /// Whether the measurement is complete
  final bool isCompleted;

  /// Whether an error occurred
  final bool hasError;

  /// Status message or error description
  final String message;

  /// Measurement progress (0-100) for devices that support it
  final int progress;

  /// Body temperature in Celsius (AOJ-20A)
  final double temperature;

  /// Blood oxygen saturation percentage 0-100 (PC60FW)
  final int spo2;

  /// Heart rate in BPM (PC60FW)
  final int heartRate;

  /// Systolic blood pressure in mmHg (PC-102)
  final int systolic;

  /// Diastolic blood pressure in mmHg (PC-102)
  final int diastolic;

  /// Weight in kilograms (ICOMON)
  final double weight;

  LepuEventData({
    this.deviceType = LepuDeviceType.unknown,
    this.state = LepuConnectionState.disconnected,
    this.isConnected = false,
    this.isCompleted = false,
    this.hasError = false,
    this.message = '',
    this.progress = 0,
    this.temperature = 0.0,
    this.spo2 = 0,
    this.heartRate = 0,
    this.systolic = 0,
    this.diastolic = 0,
    this.weight = 0.0,
  });

  /// Create from JSON - unified parser for all device types
  factory LepuEventData.fromJson(dynamic json) {
    try {
      if (json == null) {
        return LepuEventData(hasError: true, message: 'Null JSON data');
      }

      final Map<String, dynamic> data;
      if (json is String) {
        data = Map<String, dynamic>.from(jsonDecode(json));
      } else if (json is Map) {
        data = Map<String, dynamic>.from(json);
      } else {
        data = <String, dynamic>{};
      }

      // Parse common fields
      final connected = _safeBool(data['isConnected']);
      final completed = _safeBool(data['isCompleted']);
      final error = _safeBool(data['hasError']);
      final msg = data['message']?.toString() ?? '';
      final prog = _safeInt(data['progress']);

      // Parse measurement values
      final temp = _safeDouble(data['temperature']);
      final sp = _safeInt(data['spo2']);
      final hr = _safeInt(data['heart_rate']);
      final sys = _safeInt(data['systolic']);
      final dia = _safeInt(data['diastolic']);
      final wt = _safeDouble(data['weight']);
      // round to 2 decimal places
      final roundedWeight = double.parse(wt.toStringAsFixed(2));

      // Determine device type
      LepuDeviceType type = LepuDeviceType.unknown;
      final typeVal = data['deviceType']?.toString();

      if (typeVal != null) {
        switch (typeVal) {
          case 'temperature':
            type = LepuDeviceType.temperature;
            break;
          case 'spo2':
            type = LepuDeviceType.spo2;
            break;
          case 'bloodPressure':
            type = LepuDeviceType.bloodPressure;
            break;
          case 'weight':
            type = LepuDeviceType.weight;
            break;
        }
      }

      // Fallback: auto-detect from data if type is missing or unknown
      if (type == LepuDeviceType.unknown) {
        if (wt > 0) {
          type = LepuDeviceType.weight;
        } else if (temp > 0) {
          type = LepuDeviceType.temperature;
        } else if (sp > 0 || hr > 0) {
          type = LepuDeviceType.spo2;
        } else if (sys > 0 || dia > 0) {
          type = LepuDeviceType.bloodPressure;
        }
      }

      // Determine connection state
      LepuConnectionState state = LepuConnectionState.disconnected;
      if (error) {
        state = LepuConnectionState.error;
      } else if (completed) {
        state = LepuConnectionState.completed;
      } else if (connected) {
        state = LepuConnectionState.connected;
      }

      return LepuEventData(
        deviceType: type,
        state: state,
        isConnected: connected,
        isCompleted: completed,
        hasError: error,
        message: msg,
        progress: prog,
        temperature: temp,
        spo2: sp,
        heartRate: hr,
        systolic: sys,
        diastolic: dia,
        weight: roundedWeight,
      );
    } catch (e) {
      return LepuEventData(hasError: true, message: 'Parse error: $e');
    }
  }

  // Safe type parsing helpers
  static bool _safeBool(dynamic value) => value == true;

  static int _safeInt(dynamic value) {
    if (value == null) return 0;
    if (value is int) return value;
    if (value is double) return value.toInt();
    return int.tryParse(value.toString()) ?? 0;
  }

  static double _safeDouble(dynamic value) {
    if (value == null) return 0.0;
    if (value is double) return value;
    if (value is int) return value.toDouble();
    return double.tryParse(value.toString()) ?? 0.0;
  }

  /// Check if this event has valid measurement data
  bool get hasData {
    switch (deviceType) {
      case LepuDeviceType.temperature:
        return temperature > 0;
      case LepuDeviceType.spo2:
        return spo2 > 0 || heartRate > 0;
      case LepuDeviceType.bloodPressure:
        return systolic > 0 || diastolic > 0;
      case LepuDeviceType.weight:
        return weight > 0;
      case LepuDeviceType.unknown:
        return false;
    }
  }

  /// Convert to JSON
  Map<String, dynamic> toJson() => {
        'deviceType': deviceType.name,
        'isConnected': isConnected,
        'isCompleted': isCompleted,
        'hasError': hasError,
        'message': message,
        'progress': progress,
        'temperature': temperature,
        'spo2': spo2,
        'heart_rate': heartRate,
        'systolic': systolic,
        'diastolic': diastolic,
        'weight': weight,
      };

  @override
  String toString() {
    switch (deviceType) {
      case LepuDeviceType.temperature:
        return 'LepuEvent(temp: ${temperature}°C, state: ${state.name})';
      case LepuDeviceType.spo2:
        return 'LepuEvent(spo2: $spo2%, hr: $heartRate BPM, state: ${state.name})';
      case LepuDeviceType.bloodPressure:
        return 'LepuEvent(bp: $systolic/$diastolic mmHg, state: ${state.name})';
      case LepuDeviceType.weight:
        return 'LepuEvent(weight: ${weight}kg, state: ${state.name})';
      case LepuDeviceType.unknown:
        return 'LepuEvent(unknown, state: ${state.name})';
    }
  }

  /// Create a copy with modified fields
  LepuEventData copyWith({
    LepuDeviceType? deviceType,
    LepuConnectionState? state,
    bool? isConnected,
    bool? isCompleted,
    bool? hasError,
    String? message,
    int? progress,
    double? temperature,
    int? spo2,
    int? heartRate,
    int? systolic,
    int? diastolic,
    double? weight,
  }) {
    return LepuEventData(
      deviceType: deviceType ?? this.deviceType,
      state: state ?? this.state,
      isConnected: isConnected ?? this.isConnected,
      isCompleted: isCompleted ?? this.isCompleted,
      hasError: hasError ?? this.hasError,
      message: message ?? this.message,
      progress: progress ?? this.progress,
      temperature: temperature ?? this.temperature,
      spo2: spo2 ?? this.spo2,
      heartRate: heartRate ?? this.heartRate,
      systolic: systolic ?? this.systolic,
      diastolic: diastolic ?? this.diastolic,
      weight: weight ?? this.weight,
    );
  }
}
