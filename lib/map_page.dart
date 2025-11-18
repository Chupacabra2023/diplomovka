import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:google_place/google_place.dart';
import 'dart:async';
import 'event.dart'; // import novej triedy Event
import 'event_creation_page.dart';
import 'event_detail_page.dart';
import 'google_search/place_autocomplete_field.dart';
import 'event_filter_service.dart';
import 'profile_pages/user_profile.dart';
import 'profile_pages/my_visited_events_page.dart';
import 'profile_pages/my_created_events_page.dart';
import 'profile_pages/recommended_events_page.dart';
import 'profile_pages/my_invitations_page.dart';
import 'profile_pages/settings_page.dart';
import 'auth/auth_pages/logout_page.dart';
import 'profile_pages/help_page.dart';





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
  final Map<MarkerId, Event> _markerEventMap = {};

  int _nextId = 1;
  String? _selectedAdress = "";

  //this is added  by us
  bool _isPicking = false;
  LatLng? _cameraCenter;


  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  // Pridajte túto metódu do _GoogleMapPageState
  Future<void> _onPredictionSelected(AutocompletePrediction p) async {
    // Vytvorte si inštanciu GooglePlace tu, len pre túto metódu
    final googlePlace = GooglePlace("YOUR_API_KEY_HERE");
    final details = await googlePlace.details.get(p.placeId!);
    final loc = details?.result?.geometry?.location;
    _selectedAdress = details?.result?.formattedAddress;

    if (loc == null) return;

    final target = LatLng(loc.lat!, loc.lng!);
    await _controller?.animateCamera(CameraUpdate.newLatLngZoom(target, 16));

    setState(() {
      _cameraCenter = target;
    });
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
      });
    }
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Google Maps — Zoznamko'),
          actions: [
            Builder(
              builder: (context) => IconButton(
                icon: const Icon(Icons.account_circle, size: 30),
                onPressed: () {
                  Scaffold.of(context).openEndDrawer(); // teraz to funguje
                },
              ),
            ),
          ],
      ),

      endDrawer: SizedBox(
        width: MediaQuery.of(context).size.width * 0.6, // napr. 60 % šírky obrazovky
        child: Drawer(
          child: ListView(
            padding: EdgeInsets.zero,
            children: [
              const DrawerHeader(
                decoration: BoxDecoration(color: Colors.blue),
                child: Text(
                  '👤 Môj profil',
                  style: TextStyle(color: Colors.white, fontSize: 20),
                ),
              ),

              ListTile(
                leading: const Icon(Icons.person),
                title: const Text('Profil'),
                onTap: () {
                  Navigator.push(context,
                      MaterialPageRoute(builder: (context) => const ProfileSettingsPage())
                  );
                }
              ),
              ExpansionTile(
                leading: const Icon(Icons.event),
                title: const Text('Moje udalosti'),
                children: [
                  ListTile(
                    leading: const Icon(Icons.add_circle_outline),
                    title: const Text('Vytvorené'),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (context) => const MyCreatedEventsPage()),
                      );
                    },
                  ),
                  ListTile(
                    leading: const Icon(Icons.check_circle_outline),
                    title: const Text('Navštívené'),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (context) => const MyVisitedEventsPage()),
                      );
                    },
                  ),

                  ListTile(
                    leading: const Icon(Icons.star),
                    title: const Text('Oblubene'),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (context) => const RecommendedEventsPage()),
                      );
                    },
                  ),
                  ListTile(
                    leading: const Icon(Icons.mail),
                    title: const Text('Pozvánky'),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (context) => const MyInvitationsPage()),
                      );
                    },
                  ),
                ],
              ),

              ListTile(
                leading: const Icon(Icons.settings),
                title: const Text('Nastavenia'),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => const SettingsPage()),
                  );
                },
              ),
              ListTile(
                leading: const Icon(Icons.logout),
                title: const Text('Odhlásiť sa'),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => const LogoutPage()),
                  );
                },
              ),
              ListTile(
                leading: const Icon(Icons.support),
                title: const Text('Pomoc'),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => const HelpPage()),
                  );
                },
              ),
            ],
          ),
        ),
      ),
      // 👉 tieto dve veci sú mimo body!
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
      floatingActionButton: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          // Vytvoriť udalosť
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

          // Ak je v režime výberu — zobrazí sa tlačidlo ZRUŠIŤ
          if (_isPicking)
            FloatingActionButton.extended(
              heroTag: "cancelPick",
              backgroundColor: Colors.redAccent,
              icon: const Icon(Icons.close),
              label: const Text('Zrušiť'),
              onPressed: () {
                setState(() {
                  _isPicking = false;
                  _cameraCenter = null;
                });
              },
            )
          else
          // Štandardné tlačidlo "Nájsť udalosť"
            FloatingActionButton.extended(
              heroTag: "findEvent",
              icon: const Icon(Icons.search),
              label: const Text('Nájsť udalosť'),
              onPressed: () {
                _openFilterSheet();
              },
            ),
        ],
      ),

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
                child: PlaceAutocompleteField(
                  onPredictionSelected: (prediction) {
                    _onPredictionSelected(prediction);
                  },
                ),
              ),
            ),
        ],
      ),
    );
  }



  //event filtering
  void _openFilterSheet() {
    final TextEditingController nameController = TextEditingController();
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        String selectedCategory = '';
        String selectedVisibility = 'public';
        double maxPrice = 0.0;
        String eventName = 'wtf';
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
                      controller: nameController,
                      decoration: const InputDecoration(
                        hintText: "Zadaj názov...",
                        border: OutlineInputBorder(),
                      ),



                    ),
                    const SizedBox(height: 16),

                    // KATEGÓRIA
                    const Text("Kategória"),
                    DropdownButton<String>(
                      value: selectedCategory.isEmpty ? null : selectedCategory,
                      hint: const Text("Vyber kategóriu"),
                      items: [ "Hudba",
                        "Šport",
                        "Party",
                        "Kultúrne podujatia", // zahŕňa Kino, Divadlo, Festivaly
                        "Jedlo a pitie",
                        "Gaming",
                        "Príroda",
                        "Rodina",
                        "Umenie",
                        "Fotografia",
                        "Zdravie a fitness",
                        "Dobrovoľníctvo",
                        "Workshop",
                        "Diskusia",
                        "Iné"]
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
                      max: 200,
                      divisions: 200,
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
                            final name = nameController.text.trim();
                            _applyFilter(
                              name: name.isNotEmpty ? name : null,
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

    // 1. Vytvoríme objekt s kritériami
    final criteria = FilterCriteria(
      name: name,
      category: category,
      participants: participants,
      maxPrice: maxPrice,
      visibility: visibility,
      dateFrom: dateFrom,
      dateTo: dateTo,
    );

    // 2. Vytvoríme servisnú triedu
    final filterService = EventFilterService(criteria);

    // 3. Získame všetky udalosti a aplikujeme filter
    final allEvents = _markerEventMap.values.toList();
    final filteredEvents = filterService.filter(allEvents);

    // 4. Z vyfiltrovaných udalostí vytvoríme markery
    final Set<Marker> visibleMarkers = filteredEvents.map((event) {
      return event.toMarker(onTap: (e) {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => EventDetailPage(event: e)),
        );
      });
    }).toSet();

    // 5. Aktualizujeme mapu
    setState(() {
      _markers
        ..clear()
        ..addAll(visibleMarkers);
    });
  }
}