import 'package:flutter/material.dart';

class HelpPage extends StatelessWidget {
  const HelpPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Pomoc a podpora')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text(
            'Najčastejšie otázky',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          _faqItem('Ako môžem zmeniť heslo?',
              'Choď do Nastavení a klikni na „Zmeniť heslo“.'),
          _faqItem('Ako označím udalosť ako obľúbenú?',
              'Klikni na hviezdičku pri udalosti.'),
          _faqItem('Ako zruším svoj účet?',
              'V Nastaveniach dole klikni na „Odstrániť účet“.'),
          const SizedBox(height: 20),

          const Text(
            'Kontaktuj nás',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),
          ListTile(
            leading: const Icon(Icons.email_outlined),
            title: const Text('E-mail'),
            subtitle: const Text('support@mojaappka.sk'),
            onTap: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Otváram e-mailový klient...')),
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.web),
            title: const Text('Webová stránka'),
            subtitle: const Text('www.mojaappka.sk/podpora'),
          ),
        ],
      ),
    );
  }

  Widget _faqItem(String question, String answer) {
    return ExpansionTile(
      leading: const Icon(Icons.help_outline),
      title: Text(question, style: const TextStyle(fontWeight: FontWeight.bold)),
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Text(answer),
        ),
      ],
    );
  }
}
