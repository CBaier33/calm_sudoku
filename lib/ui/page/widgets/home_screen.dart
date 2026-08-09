import 'package:flutter/material.dart';
import 'package:sudoku/ui/core/widgets/page_route.dart';
import 'package:sudoku/ui/core/widgets/simple_button.dart';
import 'package:sudoku/ui/page/widgets/options_screen.dart';
import 'package:sudoku/ui/page/widgets/stat_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();

    precacheImage(
      const AssetImage('assets/sudoku_stars.png'),
      context,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              Expanded(
                child: Center(
                  child: SizedBox(
                    height: 300,
                    width: 300,
                    child: Image.asset(
                      'assets/sudoku_stars.png',
                      fit: BoxFit.contain,
                    ),
                  ),
                ),
              ),
              Column(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                spacing: 16,
                children: [
                  SimpleButton(
                    text: "Play",
                    filled: true,
                    onPressed: () {
                      Navigator.push(
                        context,
                        SimplePageRoute<void>(
                          builder: (context) => OptionsScreen(),
                        ),
                      );
                    },
                    onLongPress: () {},
                  ),
                  SimpleButton(
                    text: "Statistics",
                    filled: false,
                    onPressed: () {
                      Navigator.push(
                        context,
                        SimplePageRoute<void>(
                          builder: (context) => StatScreen(),
                        ),
                      );
                    },
                    onLongPress: () {},
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
