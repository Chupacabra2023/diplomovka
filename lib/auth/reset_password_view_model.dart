import 'package:flutter/foundation.dart';
import 'package:diplomovka/auth/reset_password_use_case.dart';
import 'package:firebase_auth/firebase_auth.dart';

class ResetPasswordViewModel extends ChangeNotifier {
  // Use Case je injektovaný v konštruktore (z KROKU 2)
  final ResetPasswordUseCase _resetPasswordUseCase;

  ResetPasswordViewModel(this._resetPasswordUseCase);

  String? _error;
  bool _isLoading = false;

  String? get error => _error;
  bool get isLoading => _isLoading;

  /// Pošle žiadosť o reset hesla a spracuje odpoveď.
  /// Vráti true, ak bolo odoslanie úspešné.
  Future<bool> sendResetEmail(String email) async {
    if (_isLoading) return false;

    _isLoading = true;
    _error = null;
    notifyListeners(); // Upozorníme UI na začiatok načítania

    try {
      // Volanie logiky (Domain Layer)
      await _resetPasswordUseCase.execute(email);
      // Úspech
      return true;
    } on FirebaseAuthException catch (e) {
      // Spracovanie špecifických chýb Firebase
      if (e.code == 'user-not-found') {
        _error = 'Používateľ s týmto e-mailom neexistuje.';
      } else if (e.code == 'invalid-email') {
        _error = 'Neplatný formát e-mailu.';
      } else {
        _error = 'Chyba: ${e.message}';
      }
      return false; // Chyba
    } catch (e) {
      _error = 'Nastala neočakávaná chyba pri odosielaní. Skúste znova.';
      return false; // Chyba
    } finally {
      _isLoading = false;
      notifyListeners(); // Upozorníme UI na koniec načítania
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}