import 'package:diplomovka/auth/login_page.dart';
import 'package:flutter/material.dart';
import 'login_service.dart';
import 'user_credentials.dart';
import '../map_page.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _confirmCtrl = TextEditingController();
  final _service = LoginService();

  void _register(){
    if (_passwordCtrl.text != _confirmCtrl.text){
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('❌ Hesla sa nezhodujú')),
      );
      return;
    }
    else{
      final credentials = UserCredentials(email: _emailCtrl.text, password: _passwordCtrl.text);
      if (_service.register(credentials)){
        Navigator.push(context, MaterialPageRoute(builder: (context) => const LoginPage() )
        );
      }
    }
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Registrácia')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(
              controller: _emailCtrl,
              decoration: const InputDecoration(
                labelText: 'Email',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _passwordCtrl,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Heslo',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _confirmCtrl,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Potvrď heslo',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () {
                _register();
              },
              style: ElevatedButton.styleFrom(
                minimumSize: const Size(double.infinity, 50),
              ),
              child: const Text('Registrovať'),
            ),
          ],
        ),
      ),
    );
  }
}
