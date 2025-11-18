import 'package:flutter/material.dart';
// import '../user_credentials.dart'; // Tento import som odstránil, ak nie je používaný.
import 'package:diplomovka/auth/auth_service.dart';
import 'package:diplomovka/auth/login_view_model.dart';
import '../../map_page.dart'; // Predpokladám, že MapPage je GoogleMapPage
import 'reset_password_page.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() {
    return _LoginPageState();
  }
}
class _LoginPageState extends State<LoginPage> {

  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();

  // 1. Zmena: Priama inicializácia ViewModelu (Lepší Dart prístup)
  final _vm = LoginViewModel(AuthService());

  @override
  void initState() {
    super.initState();

    // V initState pripojíme iba listener
    _vm.addListener(() {
      // Stav sa zmenil (napr. error alebo loading), prekreslíme UI
      if (mounted) {
        setState(() {});
      }
    });
  }

  @override
  void dispose() {
    _emailCtrl.dispose();
    _passwordCtrl.dispose();
    _vm.dispose(); // Nezabudnúť uvoľniť ViewModel
    super.dispose();
  }

  Future<void> _login() async {
    // 2. Zmena: Odstránená redundantná kontrola null

    // Spustí login logiku.
    await _vm.login(_emailCtrl.text.trim(), _passwordCtrl.text.trim());

    if (!mounted) return;

    // Zobrazenie chyby, ak nastala (ViewModel ju už spracoval na pekný text)
    if (_vm.error != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('❌ ${_vm.error}')),
      );
      // Nezabudneme vyčistiť chybu po zobrazení, ak používame dialog
      // _vm.clearError();
      return;
    }

    // Úspech
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (context) => const GoogleMapPage()),
    );
  }

  @override
  Widget build(BuildContext context) {
    // Často sa pri ViewModeloch ukladá stav premennej, aby bol kód čitateľnejší.
    final bool isLoading = _vm.isLoading;

    return Scaffold(
      appBar: AppBar(title: const Text('Prihlásenie')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(
              controller: _emailCtrl,
              decoration: const InputDecoration(labelText: 'Email'),
              keyboardType: TextInputType.emailAddress, // Dobre pre UX
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _passwordCtrl,
              decoration: const InputDecoration(labelText: 'Heslo'),
              obscureText: true,
            ),
            const SizedBox(height: 24),

            // 3. Zmena: Pridaný UX pre Loading stav
            ElevatedButton(
              // Deaktivuje tlačidlo, kým prebieha načítanie
              onPressed: isLoading ? null : _login,
              child: isLoading
                  ? const SizedBox(
                height: 20,
                width: 20,
                // Indikátor s bielou farbou pre lepšiu viditeľnosť na tmavom tlačidle
                child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
              )
                  : const Text('Prihlásiť sa'),
            ),

            TextButton(
              onPressed: isLoading ? null : () { // Deaktivujeme aj toto tlačidlo, ak sa načíta
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const ResetPasswordPage()),
                );
              },
              child: const Text(
                  'Zabudol som heslo',
                  style: TextStyle(color: Colors.indigo)
              ),
            ),
          ],
        ),
      ),
    );
  }
}