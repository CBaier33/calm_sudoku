import 'package:flutter/material.dart';

class SimpleAppBar extends StatelessWidget implements PreferredSizeWidget {
  const SimpleAppBar({super.key, required this.title});

  final String title;

  @override // Not used but needed to override to implement PreferredSizeWidget
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);

  @override
  Widget build(BuildContext context) {
    return AppBar(
      leadingWidth: 38,
      leading: SizedBox(
        width: 36,
        height: 36,
        child: IconButton(
          iconSize: 36,
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
          hoverColor: Colors.transparent,
          splashColor: Colors.transparent,
          style: IconButton.styleFrom(overlayColor: Colors.transparent),
        ),
      ),
      title: Text(title, style: Theme.of(context).textTheme.titleMedium),
      backgroundColor: Colors.white,
      shape: const Border(bottom: BorderSide(color: Colors.black, width: 3.0)),
    );
  }
}
