import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'user_credentials.dart';
import 'login_service.dart';
import '../map_page.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState(){
    return _LoginPageState();
  }
}


class _LoginPageState extends State<LoginPage> {

  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _service = LoginService();

  void _login(){
    final credentials = UserCredentials(email: _emailCtrl.text, password: _passwordCtrl.text);
    if (_service.login(credentials)){
      Navigator.push(context, MaterialPageRoute(builder: (context) => const GoogleMapPage() )
      );
    }else{
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('✅ Prihlásenie úspešné')),
      );
    }


  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('login')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          TextField(controller: _emailCtrl,
              decoration: const InputDecoration(labelText: 'Email')),
          const SizedBox(height: 12),
          TextField(controller: _passwordCtrl,
              decoration: const InputDecoration(labelText: 'Heslo'),
              obscureText: true),
          const SizedBox(height: 24),
          ElevatedButton(onPressed: _login, child: const Text('Prihlásiť sa')),
        ],
      ),
    ),);

  }
}

