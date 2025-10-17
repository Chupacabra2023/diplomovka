import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:google_place/google_place.dart';
import 'dart:async';
import 'event.dart'; // import novej triedy Event
import 'event_create_information.dart';
import 'event_detail_page.dart';

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

  String? _filterName;
  String? _filterCategory;
  int? _filterParticipants;
  double? _filterMaxPrice;
  String? _filterVisibility;
  DateTime? _filterDateFrom;
  DateTime? _filterDateTo;
  final Set<Marker> _markers = {};
  final Map<MarkerId, Event> _markerEventMap = {};

  int _nextId = 1;
  String? _selectedAdress = "";

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
    _googlePlace = GooglePlace(
        "YOUR_API_KEY_HERE"); // vlož svoj API key
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
    _debounce = Timer(const Duration(milliseconds: 300), () async {
      final text = _searchCtrl.text.trim();
      if (text.isEmpty) {
        setState(() => _predictions = []);
        return;
      }
      final result = await _googlePlace.autocomplete.get(
        text,
        language: "sk",
        components: [Component("country", "sk")],
        types: "address",
      );
      setState(() {
        _predictions = result?.predictions ?? [];
      });
    });
  }


  Future<void> _selectPrediction(AutocompletePrediction p) async {
    final details = await _googlePlace.details.get(p.placeId!);
    final loc = details?.result?.geometry?.location;
    _selectedAdress = details?.result?.formattedAddress;

    if (loc == null) return;

    final target = LatLng(loc.lat!, loc.lng!);
    await _controller?.animateCamera(CameraUpdate.newLatLngZoom(target, 16));

    setState(() {
      _cameraCenter = target;
      _predictions = [];
      _searchCtrl.text = p.description ?? "";
    });

    _searchFocus.unfocus();
  }


  void _confirmEvent() async {
    if (_cameraCenter == null) return;

    final id = 'event_${_nextId++}';
    final tempEvent = Event(
      id: id,
      title: 'Udalosť $id',
      latitude: _cameraCenter!.latitude,
      longitude: _cameraCenter!.longitude,
      createdAt: DateTime.now(),
      place: _selectedAdress ?? '',
    );

    final updatedEvent = await Navigator.push<Event>(
      context,
      MaterialPageRoute(
        builder: (context) => EventCreationInformation(event: tempEvent),
      ),
    );

    if (updatedEvent != null) {
      setState(() {
        final marker = updatedEvent.toMarker(
          onTap: (event) {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => EventDetailPage(event: event),
              ),
            );
          },
        );

        _markers.add(marker);
        _markerEventMap[marker.markerId] = updatedEvent;
        _isPicking = false;
        _predictions = [];
      });
    }
  }


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
                          hintText: 'Zadaj adresu...',
                          prefixIcon: Icon(Icons.search),
                          border: InputBorder.none,
                          contentPadding: EdgeInsets.symmetric(
                              horizontal: 12, vertical: 14),
                        ),
                      ),
                      if (_predictions.isNotEmpty)
                        ConstrainedBox(
                          constraints: const BoxConstraints(maxHeight: 220),
                          child: ListView.builder(
                            shrinkWrap: true,
                            itemCount: _predictions.length,
                            itemBuilder: (context, i) {
                              final p = _predictions[i];
                              return ListTile(
                                dense: true,
                                leading: const Icon(Icons.place),
                                title: Text(p.structuredFormatting?.mainText ??
                                    p.description ?? ''),
                                subtitle: Text(
                                    p.structuredFormatting?.secondaryText ??
                                        ''),
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
      floatingActionButton: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          FloatingActionButton.extended(
            heroTag: "createEvent",
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
          FloatingActionButton.extended(
            heroTag: "findEvent",
            icon: const Icon(Icons.search),
            label: const Text('Nájsť udalosť'),
            onPressed: () {
              _openFilterSheet(); // otvorí filter
            },
          ),
        ],
      ),

      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
    );
  }

  //event filtering
  void _openFilterSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        String selectedCategory = '';
        String selectedVisibility = 'public';
        double maxPrice = 100.0;
        String eventName = '';
        int participants = 0;
        DateTime? dateFrom;
        DateTime? dateTo;

        return StatefulBuilder(
          builder: (context, setModalState) {
            Future<void> pickDate(bool isFrom) async {
              final date = await showDatePicker(
                context: context,
                initialDate: DateTime.now(),
                firstDate: DateTime(2020),
                lastDate: DateTime(2030),
              );
              if (date != null) {
                final time = await showTimePicker(
                  context: context,
                  initialTime: TimeOfDay.now(),
                );
                if (time != null) {
                  final fullDate = DateTime(
                    date.year,
                    date.month,
                    date.day,
                    time.hour,
                    time.minute,
                  );
                  setModalState(() {
                    if (isFrom) {
                      dateFrom = fullDate;
                    } else {
                      dateTo = fullDate;
                    }
                  });
                }
              }
            }

            return Padding(
              padding: EdgeInsets.only(
                left: 16,
                right: 16,
                top: 16,
                bottom: MediaQuery
                    .of(context)
                    .viewInsets
                    .bottom + 16,
              ),
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      "🔍 Filtrovať udalosti",
                      style: TextStyle(
                          fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 16),

                    // NÁZOV
                    const Text("Názov udalosti"),
                    TextField(
                      decoration: const InputDecoration(
                        hintText: "Zadaj názov...",
                        border: OutlineInputBorder(),
                      ),
                      onChanged: (val) => setModalState(() => eventName = val),
                    ),
                    const SizedBox(height: 16),

                    // KATEGÓRIA
                    const Text("Kategória"),
                    DropdownButton<String>(
                      value: selectedCategory.isEmpty ? null : selectedCategory,
                      hint: const Text("Vyber kategóriu"),
                      items: ['Hudba', 'Šport', 'Kultúra', 'Iné']
                          .map((cat) =>
                          DropdownMenuItem(value: cat, child: Text(cat)))
                          .toList(),
                      onChanged: (val) =>
                          setModalState(() => selectedCategory = val ?? ''),
                    ),
                    const SizedBox(height: 16),

                    // POČET OSÔB
                    Text("Počet osôb: $participants"),
                    Slider(
                      value: participants.toDouble(),
                      min: 0,
                      max: 100,
                      divisions: 100,
                      label: "$participants",
                      onChanged: (val) =>
                          setModalState(() => participants = val.toInt()),
                    ),
                    const SizedBox(height: 16),

                    // DATUM OD - DO
                    const Text("Dátum a čas"),
                    Row(
                      children: [
                        Expanded(
                          child: ElevatedButton(
                            onPressed: () => pickDate(true),
                            child: Text(dateFrom == null
                                ? "Vyber OD"
                                : "Od: ${dateFrom!.day}.${dateFrom!
                                .month}. ${dateFrom!.hour}:${dateFrom!.minute
                                .toString().padLeft(2, '0')}"),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: ElevatedButton(
                            onPressed: () => pickDate(false),
                            child: Text(dateTo == null
                                ? "Vyber DO"
                                : "Do: ${dateTo!.day}.${dateTo!
                                .month}. ${dateTo!.hour}:${dateTo!.minute
                                .toString().padLeft(2, '0')}"),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),

                    // CENA
                    Text("Max cena: ${maxPrice.toStringAsFixed(0)} €"),
                    Slider(
                      value: maxPrice,
                      min: 0,
                      max: 500,
                      divisions: 50,
                      label: "${maxPrice.toStringAsFixed(0)} €",
                      onChanged: (val) => setModalState(() => maxPrice = val),
                    ),
                    const SizedBox(height: 16),

                    // VIDITEĽNOSŤ
                    const Text("Viditeľnosť"),
                    Row(
                      children: [
                        Expanded(
                          child: RadioListTile(
                            title: const Text("Verejná"),
                            value: 'public',
                            groupValue: selectedVisibility,
                            onChanged: (val) =>
                                setModalState(() => selectedVisibility = val!),
                          ),
                        ),
                        Expanded(
                          child: RadioListTile(
                            title: const Text("Súkromná"),
                            value: 'private',
                            groupValue: selectedVisibility,
                            onChanged: (val) =>
                                setModalState(() => selectedVisibility = val!),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),

                    // TLAČIDLÁ
                    Row(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        TextButton(
                          onPressed: () => Navigator.pop(context),
                          child: const Text("Zrušiť"),
                        ),
                        ElevatedButton(
                          onPressed: () {
                            Navigator.pop(context);
                            _applyFilter(
                              name: eventName,
                              category: selectedCategory,
                              participants: participants,
                              maxPrice: maxPrice,
                              visibility: selectedVisibility,
                              dateFrom: dateFrom,
                              dateTo: dateTo,
                            );
                            print(
                                "Filter → Názov: $eventName, Kat: $selectedCategory, Osoby: $participants, Cena ≤ $maxPrice €, Vid: $selectedVisibility, Od: $dateFrom, Do: $dateTo");
                          },
                          child: const Text("Použiť"),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  void _applyFilter({
    String? name,
    String? category,
    int? participants,
    double? maxPrice,
    String? visibility,
    DateTime? dateFrom,
    DateTime? dateTo,
  }) {
    setState(() {
      _filterName = name;
      _filterCategory = category;
      _filterParticipants = participants;
      _filterMaxPrice = maxPrice;
      _filterVisibility = visibility;
      _filterDateFrom = dateFrom;
      _filterDateTo = dateTo;
    });

    // Prejdeme všetky eventy
    final allEvents = _markerEventMap.values.toList();

    // Nájdi markery, ktoré spĺňajú filter
    final Set<Marker> visibleMarkers = {};
    for (final event in allEvents) {
      final matches = _matchesFilter(event);
      if (matches) {
        final marker = event.toMarker(onTap: (e) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => EventDetailPage(event: e)),
          );
        });
        visibleMarkers.add(marker);
      }
    }

    // Nevymazávame markery úplne, len ich „vizuálne nahradíme“ tými, ktoré sú viditeľné
    setState(() {
      _markers
        ..clear()
        ..addAll(visibleMarkers);
    });
  }

  /// Pomocná funkcia, ktorá kontroluje, či event spĺňa aktuálne filtre
  bool _matchesFilter(Event event) {
    if (_filterName != null &&
        _filterName!.isNotEmpty &&
        !event.title.toLowerCase().contains(_filterName!.toLowerCase())) {
      return false;
    }

    if (_filterCategory != null &&
        _filterCategory!.isNotEmpty &&
        event.category != _filterCategory) {
      return false;
    }

    if (_filterParticipants != null &&
        _filterParticipants! > 0 &&
        (event.participants ?? 0) != _filterParticipants!) {
      return false;
    }

    if (_filterMaxPrice != null &&
        (event.price ?? 0) > _filterMaxPrice!) {
      return false;
    }

    if (_filterVisibility != null &&
        _filterVisibility!.isNotEmpty &&
        event.visibility != _filterVisibility) {
      return false;
    }

    if (_filterDateFrom != null &&
        event.dateFrom != null &&
        event.dateFrom!.isBefore(_filterDateFrom!)) {
      return false;
    }

    if (_filterDateTo != null &&
        event.dateTo != null &&
        event.dateTo!.isAfter(_filterDateTo!)) {
      return false;
    }

    return true;
  }
}
