import 'package:flutter/foundation.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:diplomovka/auth/auth_service.dart';
import 'package:diplomovka/auth/login_user_use_case.dart';

/// ViewModel pre Login obrazovku.
/// Drží stav (loading + error) a volá UseCase.
class LoginViewModel extends ChangeNotifier {
  final LoginUserUseCase _loginUserUseCase;

  LoginViewModel(AuthService authService)
      : _loginUserUseCase = LoginUserUseCase(authService);

  String? _error;
  bool _isLoading = false;

  String? get error => _error;
  bool get isLoading => _isLoading;

  /// Login, ktorý volá UI (LoginPage).
  Future<void> login(String email, String password) async {
    if (_isLoading) return;

    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _loginUserUseCase.execute(email, password);
      // úspech → _error ostáva null
    } on FirebaseAuthException catch (e) {
      // Tu mapuješ technické chyby na pekné texty pre užívateľa.
      if (e.code == 'user-not-found' || e.code == 'wrong-password') {
        _error = 'Neplatný e-mail alebo heslo.';
      } else if (e.code == 'invalid-email') {
        _error = 'Neplatný formát e-mailu.';
      } else if (e.code == 'user-disabled') {
        _error = 'Tento účet bol deaktivovaný.';
      }
      // 🚀 NOVÁ KONTROLA PRE OVERENIE E-MAILU
      else if (e.code == 'email-not-verified') {
        _error = e.message; // Použijeme správu, ktorú sme definovali v UseCase

        // Môžeme mu tu hneď poslať e-mail znova
        await AuthService().sendEmailVerification();
      }
      // ------------------------------------
      else {
        _error = 'Chyba pri prihlásení: ${e.message ?? e.code}';
      }
    } catch (e) {
      _error = 'Nastala neočakávaná chyba. Skúste to znova.';
      if (kDebugMode) {
        print('LoginViewModel error: $e');
      }
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// Vyčistí error – napr. keď zavrieš dialog, nech sa UI nefixluje na starú chybu.
  void clearError() {
    _error = null;
    notifyListeners();
  }
}
