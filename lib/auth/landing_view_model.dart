import 'package:flutter/foundation.dart';
import 'package:diplomovka/auth/auth_service.dart';
// Pridaj import pre tvoj UserModel (ak ho máš definovaný)

class LandingViewModel extends ChangeNotifier {
  final AuthService _authService;

  LandingViewModel(this._authService);

  bool _isLoading = false;
  String? _error;

  bool get isLoading => _isLoading;
  String? get error => _error;

  // Vynuluje chybu po jej zobrazení na UI
  void clearError() {
    _error = null;
    notifyListeners();
  }

  // --- Implementácia Prihlásenia cez Google ---
  Future<bool> signInWithGoogle() async {
    if (_isLoading) return false;

    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final userCredential = await _authService.signInWithGoogle();

      if (userCredential != null) {
        // ✅ Úspech: Prihlásenie dokončené
        return true;
      }
      return false; // Používateľ zrušil operáciu

    } catch (e) {
      _error = "Chyba pri Google Sign-In: ${e.toString()}";
      return false;

    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  // --- Implementácia Prihlásenia cez Facebook ---
  Future<bool> signInWithFacebook() async {
   /* // Kód je podobný Google, iba volá _authService.signInWithFacebook()
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final userCredential = await _authService.signInWithFacebook();
      return userCredential != null;

    } catch (e) {
      _error = e.toString().contains('PlatformException')
          ? "Prihlásenie cez Facebook zlyhalo."
          : e.toString();
      return false;

    } finally {
      _isLoading = false;
      notifyListeners();
    }*/
    return false;
  }

  // --- Implementácia Prihlásenia cez Apple ---
 /* Future<bool> signInWithApple() async {
    // Kód je podobný, iba volá _authService.signInWithApple()
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final userCredential = await _authService.signInWithApple();
      return userCredential != null;

    } catch (e) {
      _error = e.toString().contains('UnsupportedError')
          ? "Apple Sign-In je podporovaný len na zariadeniach Apple."
          : e.toString();
      return false;

    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }*/
}