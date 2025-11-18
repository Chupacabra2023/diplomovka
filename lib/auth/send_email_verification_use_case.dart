import 'package:diplomovka/auth/auth_service.dart';

// Use Case pre iniciovanie odoslania overovacieho e-mailu po registrácii.
class SendEmailVerificationUseCase {
  final AuthService _authService;

  SendEmailVerificationUseCase(this._authService);

  /// Vykoná odoslanie overovacieho e-mailu.
  Future<void> execute() async {
    try {
      // Zavoláme metódu z Data Layer (AuthService)
      await _authService.sendEmailVerification();
    } catch (e) {
      // Chyby preposielame do ViewModelu
      rethrow;
    }
  }
}