import 'package:flutter/material.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  bool darkMode = false;
  bool notifications = true;
  String selectedLanguage = 'Slovensky';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Nastavenia')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text(
            'Všeobecné',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),

          // Tmavý režim
          SwitchListTile(
            title: const Text('Tmavý režim'),
            value: darkMode,
            onChanged: (val) {
              setState(() => darkMode = val);
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text(val ? 'Tmavý režim zapnutý' : 'Tmavý režim vypnutý')),
              );
            },
          ),

          // Notifikácie
          SwitchListTile(
            title: const Text('Notifikácie'),
            value: notifications,
            onChanged: (val) {
              setState(() => notifications = val);
            },
          ),

          // Jazyk
          ListTile(
            title: const Text('Jazyk'),
            subtitle: Text(selectedLanguage),
            trailing: const Icon(Icons.language),
            onTap: () {
              _showLanguageDialog();
            },
          ),

          const Divider(),

          // Odhlásenie
          ListTile(
            leading: const Icon(Icons.logout, color: Colors.red),
            title: const Text('Odhlásiť sa'),
            onTap: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Odhlasujem...')),
              );
              // Tu neskôr pridáme logiku odhlásenia
            },
          ),
        ],
      ),
    );
  }

  void _showLanguageDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Vyber jazyk'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: ['Slovensky', 'English', 'Deutsch'].map((lang) {
            return RadioListTile(
              title: Text(lang),
              value: lang,
              groupValue: selectedLanguage,
              onChanged: (val) {
                setState(() => selectedLanguage = val!);
                Navigator.pop(context);
              },
            );
          }).toList(),
        ),
      ),
    );
  }
}
