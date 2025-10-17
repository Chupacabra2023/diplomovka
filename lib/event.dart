import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:flutter/material.dart';

class Event {
  String id;
  String title;
  double latitude;
  double longitude;
  DateTime createdAt;
  String place;
  String description;
  DateTime? dateFrom;
  DateTime? dateTo;
  double price;
  int participants;
  String visibility;
  String category;

  Event({
    required this.id,
    required this.title,
    required this.latitude,
    required this.longitude,
    required this.createdAt,
    required this.place,
    this.description = '',
    this.dateFrom,
    this.dateTo,
    this.price = 0.0,
    this.participants = 0,
    this.visibility = 'public',
    this.category = '',
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
      'place': place,
      'description': description,
      'dateFrom': dateFrom?.toIso8601String(),
      'dateTo': dateTo?.toIso8601String(),
      'price': price,
      'participants': participants,
      'visibility': visibility,
      'category': category,
    };
  }

  factory Event.fromMap(Map<String, dynamic> map) {
    return Event(
      id: map['id'],
      title: map['title'],
      latitude: map['latitude'],
      longitude: map['longitude'],
      createdAt: DateTime.parse(map['createdAt']),
      place: map['place'],
      description: map['description'] ?? '',
      dateFrom: map['dateFrom'] != null ? DateTime.parse(map['dateFrom']) : null,
      dateTo: map['dateTo'] != null ? DateTime.parse(map['dateTo']) : null,
      price: (map['price'] ?? 0).toDouble(),
      participants: (map['participants'] ?? 0).toInt(),
      visibility: map['visibility'] ?? 'public',
      category: map['category'] ?? '',
    );
  }
}
