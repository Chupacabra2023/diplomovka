import 'package:flutter/cupertino.dart';

import 'user_credentials.dart';

class LoginService {

  // simulovaná databáza používateľov (pre začiatok)
  final Map<String, String> _users = {
    "a":"a",
    "test@test.com": "heslo123",
    "marcel@example.com": "tajneheslo"
  };

  bool login(UserCredentials credentials) {
    final storedPassword = _users[credentials.email];
    if (storedPassword == null) {
      return false; // používateľ neexistuje
    }
    return storedPassword == credentials.password;
  }

  bool register(UserCredentials credentials) {
    if (_users.containsKey(credentials.email)) {
      return false; // už existuje
    }
    _users[credentials.email] = credentials.password;
    return true;
  }
}
