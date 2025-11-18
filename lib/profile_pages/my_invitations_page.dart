import 'package:flutter/material.dart';

class MyInvitationsPage extends StatefulWidget {
  const MyInvitationsPage({super.key});

  @override
  State<MyInvitationsPage> createState() => _MyInvitationsPageState();
}

class _MyInvitationsPageState extends State<MyInvitationsPage> {
  List<Map<String, dynamic>> invitations = [];

  @override
  void initState() {
    super.initState();
    _loadInvitations();
  }

  void _loadInvitations() async {
    // 👇 tu neskôr doplníme fetch z databázy (napr. Firebase)
    await Future.delayed(const Duration(seconds: 1));

    setState(() {
      invitations = [
        {'title': 'Pozvánka: Firemný večierok', 'sender': 'Marek', 'status': 'Čaká na odpoveď'},
        {'title': 'Pozvánka: Narodeninová párty', 'sender': 'Sára', 'status': 'Prijaté'},
      ];
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Moje pozvánky')),
      body: invitations.isEmpty
          ? const Center(child: CircularProgressIndicator())
          : ListView.builder(
        itemCount: invitations.length,
        itemBuilder: (context, index) {
          final invite = invitations[index];
          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: ListTile(
              leading: const Icon(Icons.mail_outline, color: Colors.blueAccent),
              title: Text(invite['title']),
              subtitle: Text('Od: ${invite['sender']}\nStav: ${invite['status']}'),
              isThreeLine: true,
              trailing: PopupMenuButton<String>(
                onSelected: (value) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Zvolené: $value')),
                  );
                },
                itemBuilder: (context) => [
                  const PopupMenuItem(value: 'Prijať', child: Text('✅ Prijať')),
                  const PopupMenuItem(value: 'Odmietnuť', child: Text('❌ Odmietnuť')),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
