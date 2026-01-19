import 'package:flutter_test/flutter_test.dart';
import 'package:sm_lepu/lepu_event_data.dart';

void main() {
  group('LepuEventData.fromJson', () {
    test('parses temperature device string', () {
      final json = {
        'deviceType': 'temperature',
        'temperature': 36.5,
      };
      final event = LepuEventData.fromJson(json);
      expect(event.deviceType, LepuDeviceType.temperature);
      expect(event.temperature, 36.5);
    });

    test('parses spo2 device string', () {
      final json = {
        'deviceType': 'spo2',
        'spo2': 98,
        'heart_rate': 75,
      };
      final event = LepuEventData.fromJson(json);
      expect(event.deviceType, LepuDeviceType.spo2);
      expect(event.spo2, 98);
      expect(event.heartRate, 75);
    });

    test('parses bloodPressure device string', () {
      final json = {
        'deviceType': 'bloodPressure',
        'systolic': 120,
        'diastolic': 80,
      };
      final event = LepuEventData.fromJson(json);
      expect(event.deviceType, LepuDeviceType.bloodPressure);
      expect(event.systolic, 120);
      expect(event.diastolic, 80);
    });

    test('parses weight device string and rounds value', () {
      // Test 1: Rounding needed
      final json1 = {
        'deviceType': 'weight',
        'weight': 75.5678,
      };
      final event1 = LepuEventData.fromJson(json1);
      expect(event1.deviceType, LepuDeviceType.weight);
      expect(event1.weight, 75.57);

      // Test 2: No rounding needed (padding check)
      // Note: double 75.0 stays 75.0, but logic ensures it was passed through formatter
      final json2 = {
        'deviceType': 'weight',
        'weight': 75,
      };
      final event2 = LepuEventData.fromJson(json2);
      expect(event2.weight, 75.00);
    });

    test('fallback to weight detection if deviceType missing', () {
      final json = {
        'weight': 70.5,
      };
      final event = LepuEventData.fromJson(json);
      expect(event.deviceType, LepuDeviceType.weight);
      expect(event.weight, 70.50);
    });

    test('handles safe type conversion (string to num)', () {
      final json = {
        'deviceType': 'temperature',
        'temperature': "37.2", // String input
      };
      final event = LepuEventData.fromJson(json);
      expect(event.temperature, 37.2);
    });

    test('handles null inputs gracefully', () {
      final json = <String, dynamic>{};
      final event = LepuEventData.fromJson(json);
      expect(event.deviceType, LepuDeviceType.unknown);
    });
  });
}
