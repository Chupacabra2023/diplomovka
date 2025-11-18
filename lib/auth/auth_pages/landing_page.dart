import 'package:flutter/material.dart';
import 'dart:async';

import '../auth_pages/login_page.dart';
import '../auth_pages/register_page.dart';
import '../landing_view_model.dart';
import '../auth_service.dart';
import '../../map_page.dart';

class LandingPage extends StatefulWidget {
  const LandingPage({super.key});

  @override
  State<LandingPage> createState() => _LandingPageState();
}

class _LandingPageState extends State<LandingPage> {
  late LandingViewModel _vm;

  @override
  void initState() {
    super.initState();
    _vm = LandingViewModel(AuthService());

    _vm.addListener(() {
      setState(() {});

      if (_vm.error != null) {
        Timer.run(() {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('❌ ${_vm.error}')),
          );
          _vm.clearError();
        });
      }
    });
  }

  @override
  void dispose() {
    _vm.dispose();
    super.dispose();
  }

  Future<void> _signInWithGoogle() async {
    final success = await _vm.signInWithGoogle();

    if (!mounted) return;

    if (success) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => const GoogleMapPage()),
      );
    }
  }

  void _signInWithFacebook() async {
   /* final success = await _vm.signInWithFacebook();

    if (!mounted) return;

    if (success) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => const GoogleMapPage()),
      );
    }*/
  }

  void _signInWithApple() async {
    /* final success = await _vm.signInWithApple();

    if (!mounted) return;

    if (success) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => const GoogleMapPage()),
      );
    }*/
  }

  @override
  Widget build(BuildContext context) {
    final isBusy = _vm.isLoading;

    return Scaffold(
      appBar: AppBar(title: const Text('Vitajte v Aplikácii')),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text(
                'Vitaj v Zoznamku',
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 24),

              // 🔹 KLASICKÝ LOGIN
              ElevatedButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const LoginPage()),
                  );
                },
                child: const Text('Prihlásiť sa (email/heslo)'),
              ),
              const SizedBox(height: 8),

              // 🔹 REGISTRÁCIA
              OutlinedButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const RegisterPage()),
                  );
                },
                child: const Text('Vytvoriť nový účet'),
              ),
              const SizedBox(height: 24),

              // GOOGLE
              ElevatedButton.icon(
                onPressed: isBusy ? null : _signInWithGoogle,
                icon: isBusy
                    ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                )
                    : const Icon(Icons.g_mobiledata, color: Colors.white),
                label: Text(
                    isBusy ? 'Prihlasujem...' : 'Prihlásiť sa cez Google'),
                style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
              ),
              const SizedBox(height: 12),

              // FACEBOOK
              ElevatedButton.icon(
                onPressed: isBusy ? null : _signInWithFacebook,
                icon: const Icon(Icons.facebook, color: Colors.white),
                label: const Text('Prihlásiť sa cez Facebook'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blueAccent.shade700,
                ),
              ),
              const SizedBox(height: 12),

              // APPLE (zatím len placeholder)
              ElevatedButton.icon(
                onPressed: isBusy ? null : _signInWithApple,
                icon: const Icon(Icons.apple, color: Colors.black),
                label: const Text('Prihlásiť sa cez Apple'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.white,
                  foregroundColor: Colors.black,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

