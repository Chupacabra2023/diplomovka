import 'package:diplomovka/auth/auth_pages/landing_page.dart';
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';

import 'package:diplomovka/auth/auth_checker.dart';
import 'map_page.dart';
import 'auth/auth_pages/login_page.dart';
import 'auth/auth_pages/register_page.dart';

void main() async {
  // Toto musí byť pred inicializáciou Firebase
  WidgetsFlutterBinding.ensureInitialized();
  // SDK nájde konfiguráciu samo vďaka google-services.json
  await Firebase.initializeApp();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Zoznamko',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
      ),
      home: const LandingPage(),
    );
  }
}


