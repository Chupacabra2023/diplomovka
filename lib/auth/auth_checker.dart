import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:diplomovka/auth/auth_service.dart';
// Uistite sa, že cesta k AuthService je správna

import 'package:diplomovka/auth/auth_pages/landing_page.dart';
import 'package:diplomovka/map_page.dart';
// !!! ZMEŇTE map_page.dart na skutočný názov vášho súboru s mapou (napr. google_map_page.dart)

class AuthChecker extends StatelessWidget {
  const AuthChecker({super.key});

  @override
  Widget build(BuildContext context) {
    // StreamBuilder počúva zmeny v stave prihlásenia (prihlásený/odhlásený)
    return StreamBuilder<User?>(
      stream: AuthService().authStateChanges,
      builder: (context, snapshot) {

        // 1. Zobrazenie Loading Screen počas čakania na stav Firebase
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Scaffold(
            body: Center(
              child: CircularProgressIndicator(), // Zobrazíme indikátor načítania
            ),
          );
        }

        // 2. Ak má dáta (používateľ != null), je prihlásený. Presmeruj na mapu.
        if (snapshot.hasData && snapshot.data != null) {
          return const GoogleMapPage();
        }

        // 3. Ak nie je prihlásený (null), zobraz LandingPage.
        return const LandingPage();
      },
    );
  }
}