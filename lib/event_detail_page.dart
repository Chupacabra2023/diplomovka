import 'package:flutter/material.dart';
import 'event.dart';
import 'event_create_information.dart';
import 'package:intl/intl.dart';

class EventDetailPage extends StatefulWidget {
  final Event event;
  const EventDetailPage({super.key, required this.event});

  @override
  State<EventDetailPage> createState() => _EventDetailPageState();
}

class _EventDetailPageState extends State<EventDetailPage> {
  final DateFormat _formatter = DateFormat('dd.MM.yyyy HH:mm');

  @override
  Widget build(BuildContext context) {
    final e = widget.event;

    return Scaffold(
      appBar: AppBar(
        title: Text(e.title),
        actions: [
          IconButton(
            icon: const Icon(Icons.close),
            tooltip: 'Zatvoriť',
            onPressed: () => Navigator.pop(context),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text("🆔 ID: ${e.id}"),
            const SizedBox(height: 8),
            Text("📍 Miesto: ${e.place}", style: const TextStyle(fontSize: 16)),
            Text("🌍 Poloha: ${e.latitude}, ${e.longitude}"),
            const Divider(height: 20),

            Text("📝 Popis: ${e.description.isNotEmpty ? e.description : '—'}"),
            const SizedBox(height: 8),
            Text("📅 Vytvorené: ${_formatter.format(e.createdAt)}"),
            if (e.dateFrom != null)
              Text("⏰ Od: ${_formatter.format(e.dateFrom!)}"),
            if (e.dateTo != null)
              Text("⏰ Do: ${_formatter.format(e.dateTo!)}"),
            const Divider(height: 20),

            Text("💰 Cena: ${e.price.toStringAsFixed(2)} €"),
            Text("👥 Účastníci: ${e.participants}"),
            Text("👁️ Viditeľnosť: ${e.visibility}"),
            Text("🏷️ Kategória: ${e.category.isNotEmpty ? e.category : '—'}"),
            const SizedBox(height: 24),

            Center(
              child: ElevatedButton.icon(
                icon: const Icon(Icons.edit),
                label: const Text("Upraviť"),
                onPressed: () async {
                  await Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) =>
                          EventCreationInformation(event: e),
                    ),
                  );
                  setState(() {}); // refresh po návrate
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
