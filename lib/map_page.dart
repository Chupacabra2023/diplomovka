// lib/map_page.dart
import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class GoogleMapPage extends StatefulWidget {
  const GoogleMapPage({super.key});

  @override
  State<GoogleMapPage> createState() => _GoogleMapPageState();
}

class _GoogleMapPageState extends State<GoogleMapPage> {
  GoogleMapController? _controller;

  static const CameraPosition _initialCamera = CameraPosition(
    target: LatLng(48.1486, 17.1077), // Bratislava
    zoom: 13,
  );

  final Set<Marker> _markers = {};
  int _nextId = 1;

  bool _isPicking = false; // režim výberu
  LatLng? _cameraCenter;   // sledujeme center kamery

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Google Maps — Zoznamko')),
      body: Stack(
        children: [
          GoogleMap(
            initialCameraPosition: _initialCamera,
            onMapCreated: (controller) => _controller = controller,
            onCameraMove: (pos) => _cameraCenter = pos.target,
            markers: _markers,
            onTap: _onMapTap,
          ),

          // stredový pin (len ikona, nehýbe sa, hýbe sa mapa pod ním)
          if (_isPicking)
            const Align(
              alignment: Alignment.center,
              child: Padding(
                padding: EdgeInsets.only(bottom: 40), // posunie ikonu hore
                child: Icon(Icons.location_pin, size: 50, color: Colors.red),
              ),
            ),

        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        icon: Icon(_isPicking ? Icons.check : Icons.event),
        label: Text(_isPicking ? 'OK' : 'Vytvoriť udalosť'),
        onPressed: () {
          if (_isPicking) {
            // potvrdiť výber
            if (_cameraCenter != null) {
              final id = 'marker_${_nextId++}';
              _markers.add(
                Marker(
                  markerId: MarkerId(id),
                  position: _cameraCenter!,
                  infoWindow: InfoWindow(title: 'Udalosť $id'),
                ),
              );
            }
            setState(() => _isPicking = false);
          } else {
            // zapnúť výber
            setState(() => _isPicking = true);
          }
        },
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
    );
  }

  void _onMapTap(LatLng pos) {
    final id = 'marker_${_nextId++}';
    final marker = Marker(
      markerId: MarkerId(id),
      position: pos,
      infoWindow: InfoWindow(
        title: 'Miesto $id',
        snippet: '${pos.latitude.toStringAsFixed(6)}, ${pos.longitude.toStringAsFixed(6)}',
      ),
      onTap: () => _showPlaceSheet(pos, 'Miesto $id', 'Pridané ťuknutím'),
    );
    setState(() => _markers.add(marker));
  }

  void _showPlaceSheet(LatLng pos, String title, String desc) {
    showModalBottomSheet(
      context: context,
      builder: (_) => Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Text(desc),
            const SizedBox(height: 8),
            Text('Lat: ${pos.latitude.toStringAsFixed(6)}, Lng: ${pos.longitude.toStringAsFixed(6)}'),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(onPressed: () => Navigator.pop(context), child: const Text('Zatvoriť')),
                ElevatedButton(
                  onPressed: () {
                    Navigator.pop(context);
                    // tu môžeš pridať edit / uloženie do DB / navigáciu
                  },
                  child: const Text('Upraviť'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
