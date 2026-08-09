import 'package:flutter/material.dart';

class SimpleButton extends StatelessWidget {
  const SimpleButton({
    super.key,
    required this.text,
    required this.filled,
    required this.onPressed,
    required this.onLongPress,
  });

  final String text;

  final bool filled;

  final Function() onPressed;

  final Function() onLongPress;

  @override
  Widget build(BuildContext context) {
    return FilledButton(
      onPressed: onPressed,
      onLongPress: onLongPress,
      style: FilledButton.styleFrom(
        backgroundColor: filled ? Colors.black : Colors.white,
        foregroundColor: filled ? Colors.white : Colors.black,
        overlayColor: Colors.transparent,
        side: BorderSide(color: Colors.black, width: 3),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
      ),
      child: Text(
        text,
        style: Theme.of(context).textTheme.displaySmall?.copyWith(
          color: filled ? Colors.white : Colors.black,
        ),
      ),
    );
  }
}
