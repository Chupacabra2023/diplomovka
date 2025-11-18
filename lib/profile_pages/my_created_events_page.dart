import 'package:flutter/material.dart';

class MyCreatedEventsPage extends StatefulWidget {
  const MyCreatedEventsPage({super.key});

  @override
  State<MyCreatedEventsPage> createState() => _MyCreatedEventsPageState();
}

class _MyCreatedEventsPageState extends State<MyCreatedEventsPage> {
  // Neskôr tu bude fetch z databázy
  List<Map<String, dynamic>> events = [];

  @override
  void initState() {
    super.initState();
    _loadEvents(); // tu bude načítanie
  }

  void _loadEvents() async {
    // 👇 tu neskôr pridáme Firebase/DB kód
    await Future.delayed(const Duration(seconds: 1)); // simulácia načítania

    setState(() {
      events = [
        {'title': 'Letný festival', 'date': '2025-07-12'},
        {'title': 'Hackathon', 'date': '2025-09-05'},
      ];
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Moje vytvorené udalosti')),
      body: events.isEmpty
          ? const Center(child: CircularProgressIndicator()) // načítava sa
          : ListView.builder(
        itemCount: events.length,
        itemBuilder: (context, index) {
          final event = events[index];
          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: ListTile(
              leading: const Icon(Icons.event),
              title: Text(event['title']),
              subtitle: Text('Dátum: ${event['date']}'),
              trailing: const Icon(Icons.arrow_forward_ios, size: 16),
              onTap: () {
                // Tu neskôr môžeš pridať detail udalosti
              },
            ),
          );
        },
      ),
    );
  }
}
