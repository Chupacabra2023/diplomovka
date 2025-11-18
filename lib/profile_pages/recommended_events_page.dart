import 'package:flutter/material.dart';

class RecommendedEventsPage extends StatefulWidget {
  const RecommendedEventsPage({super.key});

  @override
  State<RecommendedEventsPage> createState() => _RecommendedEventsPage();
}

class _RecommendedEventsPage extends State<RecommendedEventsPage> {
  List<Map<String, dynamic>> favoriteEvents = [];

  @override
  void initState() {
    super.initState();
    _loadFavoriteEvents();
  }

  void _loadFavoriteEvents() async {
    // 👇 tu neskôr doplníme načítanie z databázy
    await Future.delayed(const Duration(seconds: 1));

    setState(() {
      favoriteEvents = [
        {'title': 'Food Festival', 'date': '2025-07-10'},
        {'title': 'Gaming Expo', 'date': '2025-09-18'},
      ];
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Moje obľúbené udalosti')),
      body: favoriteEvents.isEmpty
          ? const Center(child: CircularProgressIndicator())
          : ListView.builder(
        itemCount: favoriteEvents.length,
        itemBuilder: (context, index) {
          final event = favoriteEvents[index];
          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: ListTile(
              leading: const Icon(Icons.star, color: Colors.amber),
              title: Text(event['title']),
              subtitle: Text('Dátum: ${event['date']}'),
            ),
          );
        },
      ),
    );
  }
}
