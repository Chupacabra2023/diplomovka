import 'package:flutter/material.dart';

class MyVisitedEventsPage extends StatefulWidget {
  const MyVisitedEventsPage({super.key});

  @override
  State<MyVisitedEventsPage> createState() => _MyVisitedEventsPageState();
}

class _MyVisitedEventsPageState extends State<MyVisitedEventsPage> {
  List<Map<String, dynamic>> visitedEvents = [];

  @override
  void initState() {
    super.initState();
    _loadVisitedEvents();
  }

  void _loadVisitedEvents() async {
    // 👇 tu neskôr načítame z databázy
    await Future.delayed(const Duration(seconds: 1));

    setState(() {
      visitedEvents = [
        {'title': 'Koncert Imagine Dragons', 'date': '2025-06-01'},
        {'title': 'Workshop Flutter', 'date': '2025-08-22'},
      ];
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Moje navštívené udalosti')),
      body: visitedEvents.isEmpty
          ? const Center(child: CircularProgressIndicator())
          : ListView.builder(
        itemCount: visitedEvents.length,
        itemBuilder: (context, index) {
          final event = visitedEvents[index];
          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: ListTile(
              leading: const Icon(Icons.event_available),
              title: Text(event['title']),
              subtitle: Text('Dátum: ${event['date']}'),
            ),
          );
        },
      ),
    );
  }
}
