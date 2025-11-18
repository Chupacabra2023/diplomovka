import 'package:diplomovka/auth/auth_service.dart';
import 'package:diplomovka/auth/reset_password_use_case.dart';
import 'package:diplomovka/auth/reset_password_view_model.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

// Hlavná stránka, ktorá poskytuje ViewModel
class ResetPasswordPage extends StatelessWidget {
  const ResetPasswordPage({super.key});

  @override
  Widget build(BuildContext context) {
    // Používame ChangeNotifierProvider na vytvorenie ViewModelu a injektovanie závislostí
    return ChangeNotifierProvider<ResetPasswordViewModel>(
      create: (_) => ResetPasswordViewModel(
        ResetPasswordUseCase(AuthService()),
      ),
      // Vizuálna časť stránky
      child: const _ResetPasswordView(),
    );
  }
}

class _ResetPasswordView extends StatefulWidget {
  const _ResetPasswordView();

  @override
  State<_ResetPasswordView> createState() => _ResetPasswordViewState();
}

class _ResetPasswordViewState extends State<_ResetPasswordView> {
  final _emailController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  // Funkcia, ktorá sa spustí po stlačení tlačidla
  Future<void> _sendEmail() async {
    // Overenie formulára
    if (_formKey.currentState!.validate()) {
      // Získanie ViewModelu bez počúvania (listen: false), lebo len voláme metódu
      final vm = Provider.of<ResetPasswordViewModel>(context, listen: false);
      final success = await vm.sendResetEmail(_emailController.text.trim());

      // Kontrola, či je widget stále v strome
      if (mounted) {
        if (success) {
          // Úspech – Zobrazíme správu a vrátime sa späť na prihlasovaciu stránku
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('✅ E-mail na reset hesla bol odoslaný. Skontrolujte schránku!'),
            ),
          );
          Navigator.pop(context); // Návrat na Login Page
        } else if (vm.error != null) {
          // Chyba – Zobrazíme chybovú hlášku
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('❌ ${vm.error}')),
          );
          vm.clearError();
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    // Consumer počúva zmeny vo ViewModel (najmä isLoading pre tlačidlo)
    return Scaffold(
      appBar: AppBar(
        title: const Text('Reset Hesla'),
        backgroundColor: Theme.of(context).colorScheme.primary,
        foregroundColor: Colors.white,
      ),
      body: Consumer<ResetPasswordViewModel>(
        builder: (context, vm, child) {
          return Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24.0),
              child: Form(
                key: _formKey,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: <Widget>[
                    const Text(
                      'Zabudnuté heslo',
                      style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Colors.indigo),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 30),

                    // Email input
                    TextFormField(
                      controller: _emailController,
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
                    const SizedBox(height: 30),

                    // Tlačidlo Odoslať
                    ElevatedButton(
                      // Tlačidlo je neaktívne (null), ak ViewModel hovorí, že sa načítava
                      onPressed: vm.isLoading ? null : _sendEmail,
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
                        'Odoslať e-mail',
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


