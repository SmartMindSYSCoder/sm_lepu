class EventData {
  final bool isConnected;
  final bool isCompleted;
  final int spot;
  final int heartRate;
  final double temperature;
  final int systolic;
  final int diastolic;
  final int progress;
  final int spo2; // Added as int

  EventData({
    required this.isConnected,
    required this.isCompleted,
    required this.spot,
    required this.heartRate,
    required this.temperature,
    required this.systolic,
    required this.diastolic,
    required this.progress,
    required this.spo2, // Initialize spo2 in the constructor
  });

  // Factory constructor to create an instance from JSON
  factory EventData.fromJson(Map<String, dynamic> json) {
    return EventData(
      isConnected: json['isConnected'] ?? false,
      isCompleted: json['isCompleted'] ?? false,
      spot: json['spot'] ?? 0,
      heartRate: json['heart_rate'] ?? 0,
      temperature: json['temperature']?.toDouble() ?? 0.0,
      systolic: json['systolic'] ?? 0,
      diastolic: json['diastolic'] ?? 0,
      progress: json['progress'] ?? 0,
      spo2: json['spo2'] ?? 0, // Handle spo2 as int
    );
  }

  // Method to convert to JSON (optional)
  Map<String, dynamic> toJson() {
    return {
      'isConnected': isConnected,
      'isCompleted': isCompleted,
      'spot': spot,
      'heart_rate': heartRate,
      'temperature': temperature,
      'systolic': systolic,
      'diastolic': diastolic,
      'progress': progress,
      'spo2': spo2, // Add spo2 to JSON output
    };
  }
}
