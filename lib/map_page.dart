import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:google_place/google_place.dart';
import 'dart:async';
import 'event.dart'; // import novej triedy Event
import 'event_create_information.dart';

class GoogleMapPage extends StatefulWidget {
  const GoogleMapPage({super.key});

  @override
  State<GoogleMapPage> createState() => _GoogleMapPageState();
}

//inicialization of first view
class _GoogleMapPageState extends State<GoogleMapPage> {
  GoogleMapController? _controller;
  static const _initialCamera = CameraPosition(
    target: LatLng(48.1486, 17.1077), // Bratislava
    zoom: 13,
  );

  final Set<Marker> _markers = {};
  int _nextId = 1;

  //this is added  by us
  bool _isPicking = false;
  LatLng? _cameraCenter;

  final TextEditingController _searchCtrl = TextEditingController();
  final FocusNode _searchFocus = FocusNode();

  late GooglePlace _googlePlace;
  List<AutocompletePrediction> _predictions = [];
  Timer? _debounce;

  @override
  void initState() {
    super.initState();
    _googlePlace = GooglePlace("YOUR_API_KEY_HERE"); // vlož svoj API key
    _searchCtrl.addListener(_onSearchChanged);
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _searchCtrl.dispose();
    _searchFocus.dispose();
    super.dispose();
  }

  void _onSearchChanged() {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 350), () async {
      final text = _searchCtrl.text.trim();
      if (text.isEmpty) {
        setState(() => _predictions = []);
        return;
      }
      final res = await _googlePlace.autocomplete.get(
        text,
        language: "sk",
        components: [Component("country", "sk"), Component("country", "cz")],
      );
      setState(() => _predictions = res?.predictions ?? []);
    });
  }

  Future<void> _selectPrediction(AutocompletePrediction p) async {
    final det = await _googlePlace.details.get(p.placeId!);
    final loc = det?.result?.geometry?.location;
    if (loc == null) return;

    final target = LatLng(loc.lat!, loc.lng!);
    await _controller?.animateCamera(CameraUpdate.newLatLngZoom(target, 16));
    setState(() {
      _cameraCenter = target;
      _predictions = [];
      _searchCtrl.text = det?.result?.name ?? p.description ?? "";
    });
    _searchFocus.unfocus();
  }

  void _confirmEvent() {
    if (_cameraCenter == null) return;

    final id = 'event_${_nextId++}';
    final newEvent = Event(
      id: id,
      title: 'Udalosť $id',
      latitude: _cameraCenter!.latitude,
      longitude: _cameraCenter!.longitude,
      createdAt: DateTime.now(),
    );

    setState(() {
      _markers.add(newEvent.toMarker(onTap: _showEventSheet));
      _isPicking = false;
      _predictions = [];
    });
    Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => EventCreationInformation(event: newEvent),
        ),
    );
  }
  void _showEventSheet(Event event) {
    return null;
  }
  /*void _showEventSheet(Event event) {
    showModalBottomSheet(
      context: context,
      builder: (_) => Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(event.title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Text('Lat: ${event.latitude.toStringAsFixed(6)}'),
            Text('Lng: ${event.longitude.toStringAsFixed(6)}'),
            const SizedBox(height: 8),
            Text('Vytvorené: ${event.createdAt}'),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Zavrieť'),
                ),
                ElevatedButton(
                  onPressed: () {
                    Navigator.pop(context);
                    // sem môžeš pridať napr. edit udalosti
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
*/

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
          ),

          if (_isPicking)
            const Align(
              alignment: Alignment.center,
              child: Padding(
                padding: EdgeInsets.only(bottom: 40),
                child: Icon(Icons.location_pin, size: 50, color: Colors.red),
              ),
            ),

          if (_isPicking)
            Positioned(
              top: 16,
              left: 16,
              right: 16,
              child: SafeArea(
                child: Material(
                  elevation: 4,
                  borderRadius: BorderRadius.circular(12),
                  clipBehavior: Clip.antiAlias,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      TextField(
                        controller: _searchCtrl,
                        focusNode: _searchFocus,
                        decoration: const InputDecoration(
                          hintText: 'Vyhľadaj adresu…',
                          prefixIcon: Icon(Icons.search),
                          border: InputBorder.none,
                          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 14),
                        ),
                      ),
                      if (_predictions.isNotEmpty)
                        ConstrainedBox(
                          constraints: const BoxConstraints(maxHeight: 220),
                          child: ListView.separated(
                            shrinkWrap: true,
                            itemCount: _predictions.length,
                            separatorBuilder: (_, __) => const Divider(height: 1),
                            itemBuilder: (ctx, i) {
                              final p = _predictions[i];
                              return ListTile(
                                dense: true,
                                leading: const Icon(Icons.place),
                                title: Text(p.structuredFormatting?.mainText ?? p.description ?? ''),
                                subtitle: Text(p.structuredFormatting?.secondaryText ?? ''),
                                onTap: () => _selectPrediction(p),
                              );
                            },
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        icon: Icon(_isPicking ? Icons.check : Icons.event),
        label: Text(_isPicking ? 'OK' : 'Vytvoriť udalosť'),
        onPressed: () {
          if (_isPicking) {
            _confirmEvent();
          } else {
            setState(() => _isPicking = true);
          }
        },
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
    );
  }
}
