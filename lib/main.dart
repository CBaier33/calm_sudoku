import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:sudoku/ui/page/widgets/home_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'sudoku',
      theme: ThemeData(
        colorScheme: .fromSeed(seedColor: Colors.black),
        textTheme: TextTheme(
          titleLarge: GoogleFonts.lato(
            fontWeight: FontWeight.w900,
            fontSize: 26,
          ),
          titleMedium: GoogleFonts.lato(
            fontWeight: FontWeight.w900,
            fontSize: 24,
          ),
          titleSmall: GoogleFonts.lato(
            fontWeight: FontWeight.w900,
            fontSize: 14,
          ),
          displaySmall: GoogleFonts.lato(
            fontWeight: FontWeight.w900,
            fontSize: 24,
          ),
          bodyMedium: GoogleFonts.lato(
            fontWeight: FontWeight.w900,
            fontSize: 22,
          ),
          bodySmall: GoogleFonts.lato(
            fontWeight: FontWeight.w900,
            fontSize: 18,
          ),
          // Given digits are heavy, the ones the player fills in are light.
          headlineSmall: GoogleFonts.lato(
            fontWeight: FontWeight.w900,
            fontSize: 26,
          ),
          headlineMedium: GoogleFonts.lato(
            fontWeight: FontWeight.w500,
            fontSize: 26,
          ),
          // Pencil marks
          labelSmall: GoogleFonts.lato(
            fontWeight: FontWeight.w700,
            fontSize: 11,
          ),
        ),
      ),

      home: const HomeScreen(),
      debugShowCheckedModeBanner: false,
    );
  }
}
