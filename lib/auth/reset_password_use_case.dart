import 'package:diplomovka/auth/auth_service.dart';

// Use Case pre odoslanie e-mailu na resetovanie hesla.
class ResetPasswordUseCase {
  final AuthService _authService;

  ResetPasswordUseCase(this._authService);

  // Vykoná odoslanie e-mailu na resetovanie hesla pre danú e-mailovú adresu.
  Future<void> execute(String email) async {
    try {
      // Zavoláme metódu, ktorú sme pridali v KROKU 1
      await _authService.sendPasswordResetEmail(email);
    } catch (e) {
      // Chyby preposielame do ViewModelu
      rethrow;
    }
  }
}