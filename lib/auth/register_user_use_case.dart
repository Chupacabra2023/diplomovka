import 'package:diplomovka/auth/auth_service.dart';
import 'package:firebase_auth/firebase_auth.dart';

// Use Case pre registráciu používateľa pomocou e-mailu a hesla.
class RegisterUserUseCase {
  final AuthService _authService;

  RegisterUserUseCase(this._authService);

  /// Vykoná registráciu používateľa a vráti objekt User.
  Future<User?> execute(String email, String password) async {
    try {
      // Zavoláme metódu z Data Layer na vykonanie registrácie
      // (ktorá už obsahuje aj odoslanie overovacieho e-mailu)
      final user = await _authService.registerWithEmailAndPassword(email, password);
      return user;

    } on FirebaseAuthException {
      // Necháme chybu prejsť ďalej do ViewModelu, kde ju spracujeme.
      rethrow;
    }
  }
}