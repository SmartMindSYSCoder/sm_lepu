import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sm_lepu/sm_lepu.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late SmLepu smLepu;
  final List<MethodCall> log = <MethodCall>[];

  setUp(() {
    smLepu = SmLepu();

    // Mock the MethodChannel
    const MethodChannel('sm_lepu')
        .setMockMethodCallHandler((MethodCall methodCall) async {
      log.add(methodCall);
      // Most SmLepu methods expect a boolean return
      if ([
        'checkPermission',
        'readTemp',
        'readSpo2',
        'initBP',
        'startBP',
        'stopBP',
        'initWeight'
      ].contains(methodCall.method)) {
        return true;
      }
      return null;
    });

    log.clear();
  });

  tearDown(() {
    const MethodChannel('sm_lepu').setMockMethodCallHandler(null);
  });

  test('readTemp invokes platform method', () async {
    await smLepu.readTemp();
    expect(log, hasLength(1));
    expect(log.first.method, 'readTemp');
  });

  test('checkPermission invokes platform method', () async {
    await smLepu.checkPermission();
    expect(log, hasLength(1));
    expect(log.first.method, 'checkPermission');
  });

  test('initBP invokes platform method', () async {
    await smLepu.initBP();
    expect(log, hasLength(1));
    expect(log.first.method, 'initBP');
  });

  test('startBP invokes platform method', () async {
    await smLepu.startBP();
    expect(log, hasLength(1));
    expect(log.first.method, 'startBP');
  });

  test('readSpo2 invokes platform method', () async {
    await smLepu.readSpo2();
    expect(log, hasLength(1));
    expect(log.first.method, 'readSpo2');
  });

  test('initWeight invokes platform method', () async {
    await smLepu.initWeight();
    expect(log, hasLength(1));
    expect(log.first.method, 'initWeight');
  });
}
