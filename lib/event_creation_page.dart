import 'package:flutter/material.dart';
import 'event.dart';

class EventCreationInformation extends StatefulWidget {
  final Event event;

  EventCreationInformation({super.key, required this.event});

  @override
  State<EventCreationInformation> createState() {
    return new _EventCreationInformationState();
  }
}

class _EventCreationInformationState extends State<EventCreationInformation> {
  final TextEditingController event_heading_setter = TextEditingController();
  final TextEditingController event_description_setter =
      TextEditingController();
  final TextEditingController event_category_setter = TextEditingController();
  final TextEditingController event_place_setter = TextEditingController();
  final TextEditingController event_dateandtime_setter =
      TextEditingController(); // od
  final TextEditingController event_dateandtime_end_setter =
      TextEditingController(); // do 🆕
  final TextEditingController event_price_setter = TextEditingController();
  final TextEditingController event_participants_setters =
      TextEditingController();
  final TextEditingController event_visibility_setter = TextEditingController();

  List<String> allCategories = [
    "Hudba",
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
    "Iné"
  ];
  List<String> selectedCategories = [];

  @override
  void dispose() {
    event_heading_setter.dispose();
    event_description_setter.dispose();
    event_category_setter.dispose();
    event_place_setter.dispose();
    event_dateandtime_setter.dispose();
    event_dateandtime_end_setter.dispose(); // 🆕 pridaj aj do dispose
    event_price_setter.dispose();
    event_participants_setters.dispose();
    event_visibility_setter.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Editing Event")),
      body: Padding(
        padding: EdgeInsets.all(16),
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text("NAZOV"),
              TextField(
                controller: event_heading_setter,
                decoration: const InputDecoration(
                  labelText: "Name of event",
                  border: OutlineInputBorder(),
                ),
              ),
              Text("popis"),
              TextField(
                controller: event_description_setter,
                decoration: const InputDecoration(
                  labelText: "describe your event",
                  border: OutlineInputBorder(),
                ),
              ),
              Text("kategoria"),
              Wrap(
                spacing: 8,
                children: allCategories.map((category) {
                  final isSelected = selectedCategories.contains(category);
                  return FilterChip(
                    label: Text(category),
                    selected: isSelected,
                    onSelected: (selected) {
                      setState(() {
                        if (selected) {
                          selectedCategories.add(category);
                        } else {
                          selectedCategories.remove(category);
                        }
                      });
                    },
                  );
                }).toList(),
              ),
              Text("miesto"),
              TextField(
                controller: event_place_setter,
                decoration: InputDecoration(
                  labelText: widget.event.place,
                  border: OutlineInputBorder(),
                ),
              ),
              Text("dátum a čas — od"),
              TextField(
                controller: event_dateandtime_setter,
                decoration: const InputDecoration(
                  labelText: "Vyber začiatok",
                  border: OutlineInputBorder(),
                ),
                readOnly: true,
                onTap: () async {
                  final date = await showDatePicker(
                    context: context,
                    initialDate: DateTime.now(),
                    firstDate: DateTime.now(),
                    lastDate: DateTime(2100),
                  );
                  if (date != null) {
                    final time = await showTimePicker(
                      context: context,
                      initialTime: TimeOfDay.now(),
                    );
                    if (time != null) {
                      final selected = DateTime(
                        date.year,
                        date.month,
                        date.day,
                        time.hour,
                        time.minute,
                      );
                      event_dateandtime_setter.text = selected.toString();
                    }
                  }
                },
              ),

              SizedBox(height: 12),

              Text("dátum a čas — do"),
              TextField(
                controller: event_dateandtime_end_setter, // 🆕 nový controller
                decoration: const InputDecoration(
                  labelText: "Vyber koniec",
                  border: OutlineInputBorder(),
                ),
                readOnly: true,
                onTap: () async {
                  final date = await showDatePicker(
                    context: context,
                    initialDate: DateTime.now(),
                    firstDate: DateTime.now(),
                    lastDate: DateTime(2100),
                  );
                  if (date != null) {
                    final time = await showTimePicker(
                      context: context,
                      initialTime: TimeOfDay.now(),
                    );
                    if (time != null) {
                      final selected = DateTime(
                        date.year,
                        date.month,
                        date.day,
                        time.hour,
                        time.minute,
                      );
                      event_dateandtime_end_setter.text = selected.toString();
                    }
                  }
                },
              ),

              Text("cena"),
              TextField(
                controller: event_price_setter,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: "Cena (€)",
                  border: OutlineInputBorder(),
                ),
              ),
              Text("počet osob"),
              TextField(
                controller: event_participants_setters,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: "Počet osob",
                  border: OutlineInputBorder(),
                ),
              ),
              ElevatedButton(
                onPressed: () {
                  // ✋ Validácia názvu
                  if (event_heading_setter.text.trim().isEmpty) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Prosím, zadaj názov udalosti.')),
                    );
                    return; // ⛔ zastaví ukladanie
                  }
                  widget.event.title = event_heading_setter.text;
                  widget.event.place = event_place_setter.text;
                  widget.event.description = event_description_setter.text;
                  widget.event.price =
                      double.tryParse(event_price_setter.text) ?? 0.0;
                  widget.event.participants =
                      int.tryParse(event_participants_setters.text) ?? 0;
                  widget.event.dateFrom = DateTime.tryParse(
                    event_dateandtime_setter.text,
                  );
                  widget.event.dateTo = DateTime.tryParse(
                    event_dateandtime_end_setter.text,
                  );
                  widget.event.category = selectedCategories.isNotEmpty
                      ? selectedCategories.join(', ')
                      : 'Nezaradené';


                  Navigator.pop(context, widget.event);
                },
                child: const Text("Uložiť"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
