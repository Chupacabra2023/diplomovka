import 'package:flutter/foundation.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:diplomovka/auth/auth_service.dart';
import 'package:diplomovka/auth/register_user_use_case.dart';

class RegisterViewModel extends ChangeNotifier {
  final RegisterUserUseCase _registerUserUseCase;

  RegisterViewModel(AuthService authService)
      : _registerUserUseCase = RegisterUserUseCase(authService);

  String? _error;
  bool _isLoading = false;

  String? get error => _error;
  bool get isLoading => _isLoading;

  Future<void> register(String email, String password) async {
    if (_isLoading) return;

    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _registerUserUseCase.execute(email, password);

    } on FirebaseAuthException catch (e) {
      if (e.code == 'weak-password') {
        _error = 'Heslo je príliš slabé.';
      } else if (e.code == 'email-already-in-use') {
        _error = 'Účet s týmto e-mailom už existuje.';
      } else if (e.code == 'invalid-email') {
        _error = 'Neplatný formát e-mailu.';
      } else {
        _error = 'Chyba pri registrácii: ${e.message}';
      }
    } catch (e) {
      _error = 'Nastala neočakávaná chyba. Skúste to znova.';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
