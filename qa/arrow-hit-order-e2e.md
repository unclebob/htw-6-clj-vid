# Arrow Hit Order End-to-End QA Suite

## Scope

Verify through the user interface that an arrow stops when it hits the Wumpus, even when the player appears later in the requested shot path. QA must drive the game through shell commands, terminal input, and documented observation output only.

## Required UI Affordances

- `htw` is available from the project command directory or documented installation path.
- A documented setup mode lets QA choose player, Wumpus, pit, bat, and arrow counts.
- The UI reports the win message `AHA! YOU GOT THE WUMPUS!`.
- The UI reports the self-hit message `OOPS! ARROW GOT YOU!`.
- Documented observation output lets QA verify the actual arrow path visited by the shot without calling project APIs.

## QA-1: Direct Wumpus Hit Ignores Later Player Room

1. Start `htw` with player room `1`, Wumpus room `2`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Enter `n`.
3. Shoot path `2, 1`.
4. Verify the output includes `AHA! YOU GOT THE WUMPUS!`.
5. Verify the output does not include `OOPS! ARROW GOT YOU!`.
6. Verify documented observation output shows the arrow path ended at room `2`.
7. Verify the game is won.

## QA-2: Multi-Room Wumpus Hit Ignores Later Player Room

1. Start `htw` with player room `1`, Wumpus room `10`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Enter `n`.
3. Shoot path `2, 10, 9, 8, 1`.
4. Verify the output includes `AHA! YOU GOT THE WUMPUS!`.
5. Verify the output does not include `OOPS! ARROW GOT YOU!`.
6. Verify documented observation output shows the arrow path ended at rooms `2, 10`.
7. Verify the game is won.
