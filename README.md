# Sudoku

## Background

A companion to my [minesweeper](https://github.com/CBaier33/minesweeper) app, built in the same style for the [Mudita Kompakt](https://mudita.com/) and its e-ink screen.

Every board is generated on the device. A solved grid is built first, then cells are removed one at a time and each removal is kept only if the puzzle still has exactly one solution, so there is always a way to reason your way to the answer without guessing.

## Installation

You may either install from the Releases section of this project (I recommend using [Obtainium](https://wiki.obtainium.imranr.dev/)) or you may build from source.

```sh
./scripts/setup.sh     # packages, launcher icon, splash screen
./scripts/install.sh   # build a release APK and push it to a connected device
```

### Screenshots

<table>
  <tr>
    <td align="center">
      <img src="images/home.png" width="220"><br>
    </td>
    <td align="center">
      <img src="images/options.png" width="220"><br>
    </td>
    <td align="center">
      <img src="images/game.png" width="220"><br>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="images/notes.png" width="220"><br>
    </td>
    <td align="center">
      <img src="images/win.png" width="220"><br>
    </td>
    <td align="center">
      <img src="images/loss.png" width="220"><br>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="images/stats.png" width="220"><br>
    </td>
    <td></td>
    <td></td>
  </tr>
</table>

## Play

- **Tap** a cell to select it, then tap a digit on the pad to fill it in.
- **Long press** a cell to clear it.
- **Notes** switches the pad to pencil marks, so a digit is jotted into the corner of the cell instead of being played.
- **Erase** empties the selected cell.

A wrong digit inverts the cell and costs you one of your mistakes — five on easy, three on medium and hard. Run out and the grid reveals itself. The counters across the top are cells left to solve, the mistakes you have spent, and your time. Tap the face to deal a new puzzle.

## Design

Mudita Kompakt users should find the UI familiar. Everything is pure black on white with hard borders and no motion, which is what an e-ink panel is good at: `SimplePageRoute` strips the page transition, the option toggles animate over `Duration.zero`, and the only greys in the app are the ones that mark the selected cell and its peers.

## Contributing

PRs and forks would be cool however unlikely. I've kept it simple but there are a lot of features I could see for this and perhaps web support.
