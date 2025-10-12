import 'package:flutter/material.dart';
import 'event.dart';

class EventCreationInformation extends StatelessWidget {
  final Event event;

  const EventCreationInformation({super.key, required this.event});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Upraviť udalosť')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Poloha: ${event.latitude}, ${event.longitude}'),
            const SizedBox(height: 12),
            TextField(
              decoration: const InputDecoration(labelText: 'Názov'),
              controller: TextEditingController(text: event.title),
            ),
            ElevatedButton(onPressed: (){
              Navigator.pop(context);
            }, child: const Text('Upraviť')),
          ],
        ),
      ),
    );
  }
}
