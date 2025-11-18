import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:diplomovka/auth/auth_service.dart';
import 'package:diplomovka/auth/auth_pages/login_page.dart'; // Predpokladaná cesta
import 'package:diplomovka/auth/register_user_use_case.dart'; // Potrebný import
import 'package:diplomovka/auth/register_view_model.dart'; // Potrebný import

// Hlavný widget, ktorý poskytuje ViewModel pomocou Provider
class RegisterPage extends StatelessWidget {
  const RegisterPage({super.key});

  @override
  Widget build(BuildContext context) {
    // Injektovanie ViewModel a závislostí
    return ChangeNotifierProvider<RegisterViewModel>(
      create: (context) => RegisterViewModel(
        AuthService(), // Potrebné pre RegisterUserUseCase
      ),
      child: const _RegisterView(),
    );
  }
}

// Vizuálna časť stránky
class _RegisterView extends StatefulWidget {
  const _RegisterView();

  @override
  State<_RegisterView> createState() => _RegisterViewState();
}

class _RegisterViewState extends State<_RegisterView> {
  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _confirmCtrl = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _emailCtrl.dispose();
    _passwordCtrl.dispose();
    _confirmCtrl.dispose();
    super.dispose();
  }

  Future<void> _register() async {
    // 1. KONTROLA: Validácia formulára
    if (!_formKey.currentState!.validate()) {
      return;
    }

    final vm = Provider.of<RegisterViewModel>(context, listen: false);

    // KONTROLA HESLA: Toto musí byť na UI vrstve, nie vo ViewModeli!
    if (_passwordCtrl.text.trim() != _confirmCtrl.text.trim()) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('❌ Heslá sa nezhodujú!')),
      );
      return;
    }

    // Volanie ViewModelu iba s overenými údajmi (email, password)
    await vm.register(
      _emailCtrl.text.trim(),
      _passwordCtrl.text.trim(),
    );

    // Po ukončení asynchrónnej operácie
    if (!mounted) return;

    if (vm.error != null) {
      // Zobrazenie chyby z Firebase cez ViewModel
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('❌ ${vm.error}')),
      );
      vm.clearError();
      return;
    }

    // Úspech: Navigácia na LoginPage
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('✅ Registrácia úspešná! Skontrolujte e-mail pre overenie.')),
    );
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (context) => const LoginPage()),
    );
  }

  @override
  Widget build(BuildContext context) {
    // Consumer na sledovanie stavu (isLoading)
    return Scaffold(
      appBar: AppBar(title: const Text('Registrácia'),
        backgroundColor: Theme.of(context).colorScheme.primary,
        foregroundColor: Colors.white,
      ),
      body: Consumer<RegisterViewModel>(
        builder: (context, vm, child) {
          return Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24.0),
              child: Form(
                key: _formKey,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Text(
                      'Vytvoriť nový účet',
                      style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Colors.indigo),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 30),

                    // Email input
                    TextFormField(
                      controller: _emailCtrl,
                      keyboardType: TextInputType.emailAddress,
                      decoration: const InputDecoration(
                        labelText: 'E-mail',
                        prefixIcon: Icon(Icons.email_outlined),
                        border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(12))),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty || !value.contains('@')) {
                          return 'Zadajte platný e-mail.';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // Heslo input
                    TextFormField(
                      controller: _passwordCtrl,
                      obscureText: true,
                      decoration: const InputDecoration(
                        labelText: 'Heslo (min. 6 znakov)',
                        prefixIcon: Icon(Icons.lock_outline),
                        border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(12))),
                      ),
                      validator: (value) {
                        if (value == null || value.length < 6) {
                          return 'Heslo musí mať aspoň 6 znakov.';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // Potvrdiť heslo input
                    TextFormField(
                      controller: _confirmCtrl,
                      obscureText: true,
                      decoration: const InputDecoration(
                        labelText: 'Potvrď heslo',
                        prefixIcon: Icon(Icons.lock_open),
                        border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(12))),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Potvrdenie hesla je povinné.';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 30),

                    // Tlačidlo Registrovať
                    ElevatedButton(
                      onPressed: vm.isLoading ? null : _register,
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        backgroundColor: Theme.of(context).colorScheme.primary,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      child: vm.isLoading
                          ? const SizedBox(
                        width: 24,
                        height: 24,
                        child: CircularProgressIndicator(color: Colors.white, strokeWidth: 3),
                      )
                          : const Text(
                        'Registrovať',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}