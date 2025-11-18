import 'package:flutter/material.dart';

class ProfileSettingsPage extends StatefulWidget {
  const ProfileSettingsPage({super.key});

  @override
  State<ProfileSettingsPage> createState() => _ProfileSettingsPageState();
}

class _ProfileSettingsPageState extends State<ProfileSettingsPage> {
  String profileImage = 'https://via.placeholder.com/150'; // dočasná fotka
  final nameController = TextEditingController(text: 'Tvoje meno');
  final emailController = TextEditingController(text: 'tvoj@email.com');
  final passwordController = TextEditingController(text: '********');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Nastavenia profilu')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Profilová fotka
            GestureDetector(
              onTap: () {
                // Tu neskôr pridáme ImagePicker
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Zmena profilovej fotky ešte nefunguje')),
                );
              },
              child: CircleAvatar(
                radius: 50,
                backgroundImage: NetworkImage(profileImage),
              ),
            ),
            const SizedBox(height: 20),

            // Meno
            TextField(
              controller: nameController,
              decoration: const InputDecoration(
                labelText: 'Meno',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 15),

            // Email
            TextField(
              controller: emailController,
              decoration: const InputDecoration(
                labelText: 'Email',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 15),

            // Heslo
            TextField(
              controller: passwordController,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Heslo',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 25),

            // Priatelia (zatím placeholder)
            const Align(
              alignment: Alignment.centerLeft,
              child: Text('Priatelia', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            ),
            const SizedBox(height: 10),
            Column(

            ),

            const SizedBox(height: 25),

            // Uložiť zmeny
            ElevatedButton.icon(
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Zmeny uložené (zatím len vizuálne)')),
                );
              },
              icon: const Icon(Icons.save),
              label: const Text('Uložiť zmeny'),
              style: ElevatedButton.styleFrom(
                minimumSize: const Size(double.infinity, 50),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
