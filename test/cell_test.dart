import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:sudoku/ui/game/widgets/cell.dart';

/// Renders [child] in a cell of exactly [size], the way the board's grid does.
Future<void> _pumpInCell(
  WidgetTester tester,
  Widget child, {
  required double size,
  double textScale = 1.0,
}) {
  return tester.pumpWidget(
    MaterialApp(
      home: MediaQuery(
        data: MediaQueryData(textScaler: TextScaler.linear(textScale)),
        child: Center(
          child: SizedBox(width: size, height: size, child: child),
        ),
      ),
    ),
  );
}

void main() {
  testWidgets('Pencil marks fit a cell too small for three lines of text', (
    WidgetTester tester,
  ) async {
    await _pumpInCell(tester, const Notes(notes: {1, 5, 9}), size: 20);

    expect(tester.takeException(), isNull);
  });

  testWidgets('Pencil marks fit when the user scales their text up', (
    WidgetTester tester,
  ) async {
    await _pumpInCell(
      tester,
      const Notes(notes: {1, 2, 3, 4, 5, 6, 7, 8, 9}),
      size: 40,
      textScale: 3.0,
    );

    expect(tester.takeException(), isNull);
  });

  testWidgets('Pencil marks stay inside the cell they belong to', (
    WidgetTester tester,
  ) async {
    await _pumpInCell(
      tester,
      const Notes(notes: {1, 2, 3, 4, 5, 6, 7, 8, 9}),
      size: 30,
    );

    final cell = tester.getRect(find.byType(Notes));

    // Rect.contains excludes the far edges, so compare the bounds directly - a
    // mark sitting flush against the cell border is still inside it.
    for (final mark in find.byType(Text).evaluate()) {
      final rect = tester.getRect(find.byWidget(mark.widget));
      expect(rect.left, greaterThanOrEqualTo(cell.left));
      expect(rect.top, greaterThanOrEqualTo(cell.top));
      expect(rect.right, lessThanOrEqualTo(cell.right));
      expect(rect.bottom, lessThanOrEqualTo(cell.bottom));
    }
  });
}
