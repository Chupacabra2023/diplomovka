import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:flutter/material.dart';

class Event {
  final String id;
  final String title;
  final double latitude;
  final double longitude;
  final DateTime createdAt;

  Event({
    required this.id,
    required this.title,
    required this.latitude,
    required this.longitude,
    required this.createdAt,
  });

  Marker toMarker({void Function(Event)? onTap}) {
    return Marker(
      markerId: MarkerId(id),
      position: LatLng(latitude, longitude),
      infoWindow: InfoWindow(title: title),
      onTap: () {
        if (onTap != null) onTap(this);
      },
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'title': title,
      'latitude': latitude,
      'longitude': longitude,
      'createdAt': createdAt.toIso8601String(),
    };
  }

  factory Event.fromMap(Map<String, dynamic> map) {
    return Event(
      id: map['id'],
      title: map['title'],
      latitude: map['latitude'],
      longitude: map['longitude'],
      createdAt: DateTime.parse(map['createdAt']),
    );
  }
}
