import 'package:flutter/material.dart';

class DigitDisplay extends StatelessWidget {
  const DigitDisplay({super.key, required this.data});

  final String data;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 107,
      height: 60,
      padding: EdgeInsets.all(5),
      decoration: BoxDecoration(
        border: Border.all(color: Colors.black, width: 3),
        borderRadius: BorderRadius.circular(13),
      ),
      child: Center(
        child: FittedBox(
          fit: BoxFit.scaleDown,
          child: Text(data, style: TextStyle(fontSize: 32)),
        ),
      ),
    );
  }
}

String padInt(int value) {
  final isNegative = value < 0;
  final width = (isNegative) ? 2 : 3;
  final absStr = value.abs().toString().padLeft(width, '0');
  if (value >= 999) return "999"; // Mimics the original minesweeper window
  return isNegative ? '-$absStr' : absStr;
}

String formatTime(int seconds) {
  if (seconds >= 5999) return "99:59";
  final minutes = (seconds ~/ 60).toString().padLeft(2, '0');
  return "$minutes:${(seconds % 60).toString().padLeft(2, '0')}";
}
