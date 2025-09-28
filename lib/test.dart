// lib/osm_test_page.dart
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

class OsmTestPage extends StatelessWidget {
  const OsmTestPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('OSM test')),
      body: FlutterMap(
        options: MapOptions(
          initialCenter: LatLng(48.1486, 17.1077),
          initialZoom: 13,
        ),
        children: [
          TileLayer(
            urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
            userAgentPackageName: 'com.example.diplomovka', // zmeň podľa svojho package
          ),
          // jednoduchá atribúcia (ak chceš)
          RichAttributionWidget(
            attributions: [
              TextSourceAttribution('© OpenStreetMap contributors'),
            ],
          ),
        ],
      ),
    );
  }
}
