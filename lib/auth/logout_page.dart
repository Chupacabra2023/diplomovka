import 'package:flutter/material.dart';

class LogoutPage extends StatelessWidget {
  const LogoutPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Odhlásiť sa')),
      body: Center(
        child: ElevatedButton.icon(
          icon: const Icon(Icons.logout, color: Colors.white),
          label: const Text('Odhlásiť sa'),
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.red,
            minimumSize: const Size(200, 50),
          ),
          onPressed: () {
            // Tu neskôr pridáme reálne odhlásenie (napr. Firebase Auth)
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Odhlásenie prebehlo úspešne')),
            );

            // Po odhlásení presmerovanie späť na login
            Navigator.pushAndRemoveUntil(
              context,
              MaterialPageRoute(builder: (context) => const LoginPage()),
                  (route) => false,
            );
          },
        ),
      ),
    );
  }
}

// Toto je len placeholder pre login obrazovku
class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Prihlásenie')),
      body: const Center(child: Text('Tu bude login stránka')),
    );
  }
}
