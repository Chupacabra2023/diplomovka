// lib/map_page.dart
import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

//StatefulWidget vie si pamätať a meniť stav → voláme setState().
class GoogleMapPage extends StatefulWidget {
  const GoogleMapPage({super.key});
//Každý StatefulWidget musí mať metódu createState().
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Google Maps — Zoznamko')),
      body: GoogleMap(
        initialCameraPosition: _initialCamera,
        onMapCreated: (controller) => _controller = controller,
        myLocationEnabled: false,
        myLocationButtonEnabled: false,
        markers: _markers,
        onTap: _onMapTap, // ťuknutím pridáš pin
      ),
      floatingActionButton: FloatingActionButton.extended(
        icon: const Icon(Icons.add_location_alt),
        label: const Text('Pridať pin (v strede)'),
        onPressed: _addMarkerAtCenter,
      ),
    );
  }

  void _onMapTap(LatLng pos) {
    final id = 'marker_${_nextId++}';
    final marker = Marker(
      markerId: MarkerId(id),
      position: pos,
      infoWindow: InfoWindow(title: 'Miesto $id', snippet: '${pos.latitude.toStringAsFixed(6)}, ${pos.longitude.toStringAsFixed(6)}'),
      onTap: () => _showPlaceSheet(pos, 'Miesto $id', 'Pridané ťuknutím'),
    );
    setState(() => _markers.add(marker));
  }

  void _addMarkerAtCenter() async {
    if (_controller == null) return;
    final center = await _controller!.getLatLng(ScreenCoordinate(x: MediaQuery.of(context).size.width ~/ 2, y: MediaQuery.of(context).size.height ~/ 2));
    final id = 'marker_${_nextId++}';
    final marker = Marker(
      markerId: MarkerId(id),
      position: center,
      infoWindow: InfoWindow(title: 'Moje miesto $id'),
      onTap: () => _showPlaceSheet(center, 'Moje miesto $id', 'Pridané v strede'),
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
                ElevatedButton(onPressed: () {
                  Navigator.pop(context);
                  // sem môžeš pridať edit / uloženie do DB / navigáciu
                }, child: const Text('Upraviť')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
