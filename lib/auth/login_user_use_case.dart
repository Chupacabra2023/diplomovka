// LoginUserUseCase.dart
import 'package:firebase_auth/firebase_auth.dart';
import 'package:diplomovka/auth/auth_service.dart';

/// Use Case pre prihlásenie používateľa pomocou e-mailu a hesla.
class LoginUserUseCase {
  final AuthService _authService;

  LoginUserUseCase(this._authService);

  /// Vykoná prihlásenie používateľa a skontroluje, či je e-mail overený.
  Future<void> execute(String email, String password) async {
    // 1. Prihlásenie
    await _authService.signInWithEmailAndPassword(email, password);

    // 2. Kontrola overenia e-mailu po prihlásení
    final user = _authService.currentUser;

    if (user != null && !user.emailVerified) {
      // Ak nie je overený, odhlásime ho!
      await _authService.logout();

      // Vyhodíme špeciálnu výnimku, ktorú spracujeme vo ViewModeli.
      throw FirebaseAuthException(
        code: 'email-not-verified',
        message: 'E-mail nebol overený. Skontrolujte svoju schránku a kliknite na overovací odkaz.',
      );
    }
  }
}