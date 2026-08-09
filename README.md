# Forder

The two-player abstract strategy game where the odds are never fixed.

## Gameplay

Each player is assigned a colour: red or blue.

The initial board is:

```text
R R B B R R B B
R R B B R R B B
B B R R B B R R
B B R R B B R R
R R B B R R B B
R R B B R R B B
B B R R B B R R
B B R R B B R R
```

Players alternate turns.

On a turn:

1. Choose one of the 64 cells.
2. Flip it to its opposite state.
3. The resulting board must not have appeared previously in the game.
4. If the move completes a winning row for either player, the game ends.
5. Otherwise, play passes to the opponent.

## AI

Forder includes an adversarial search engine in `ForderAI`.

The AI currently uses:

* **Minimax search**
* **Alpha–beta pruning**
* **Iterative deepening**
* **Move ordering**
* **Time-limited search**
* **History-aware move generation**
* **Heuristic evaluation at the search frontier**
* **Detection of immediate wins and tactical threats**
* **Recognition of multi-threat positions ("forks")**

### Core components

**`ForderAI`**
Search engine responsible for selecting moves using iterative-deepening minimax and alpha–beta pruning.

**`ForderRules`**
Compact rules implementation for the AI, including board manipulation, win detection and board formatting.
r
**`ForderHistory`**
Tracks previously visited board states so that repetition can be prevented both during real play and simulated search.

**`ForderBoardConverter`**
Provides conversion between the graphical board representation and the compact integer representation used by the AI.

**`Board` / `Cell`**
JavaFX-side representation of the interactive game board.

**`PatternEngine`**
Detects completed patterns on the graphical board.

**`ForderAITest`**
Command-line smoke test for exercising the AI independently of the graphical interface.

## Requirements

* **Java 21**
* **Maven**
* **JavaFX 21**

JavaFX dependencies are managed through Maven.

## Running the project

Clone the repository:

```bash
git clone https://github.com/codeuan/Forder.git
cd Forder/forder
```

Run the JavaFX application:

```bash
mvn javafx:run
```

To compile the project:

```bash
mvn clean compile
```

Author: Connor Dean-Pijuan
