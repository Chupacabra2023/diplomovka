class UserCredentials {
  final String email;
  final String password;
  final Map<String, String> _users = {
    'user1': 'password1',
    'user2': 'password2',
    'user3': 'password3',
  };

  UserCredentials({required this.email, required this.password});

  bool isValidEmail() {
    return email.contains('@') && email.contains('.');
  }

  bool isValidPassword() {
    return password.length >= 6;
  }
}
