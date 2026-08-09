import 'package:flutter/material.dart';
import 'package:sudoku/ui/core/widgets/simple_button.dart';

Future<void> actionModal(
  BuildContext context,
  Column titles,
  List<SimpleButton> buttons,
) {
  return showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.white,
    barrierColor: Colors.transparent,
    shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
    sheetAnimationStyle: AnimationStyle.noAnimation,
    enableDrag: false,
    builder: (BuildContext context) {
      return Container(
        decoration: const BoxDecoration(
          border: Border(top: BorderSide(color: Colors.black, width: 3)),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: titles,
            ),

            Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                spacing: 10,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: buttons,
              ),
            ),
          ],
        ),
      );
    },
  );
}
