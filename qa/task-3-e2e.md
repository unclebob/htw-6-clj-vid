# Task 3 End-to-End QA Suite

## Scope

Verify crooked-arrow shooting through user-visible commands and output. QA must not call project APIs or inspect private state.

## Required UI Affordances

- A documented command starts the program from a clean checkout.
- A documented seeded or scripted setup mode lets QA choose player, Wumpus, pit, bat, and arrow state.
- A documented deterministic randomness mode lets QA choose invalid-arrow deviation and Wumpus wake outcomes.
- The player can shoot with the compact command form `s <room1> [<room2> ...]`.

## QA-1: One-Room Arrow Hit

1. Start with player room `1`, Wumpus room `2`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Enter `s 2`.
3. Verify the output includes `AHA! YOU GOT THE WUMPUS!`.
4. Verify the game is won.

## QA-2: Multi-Room Crooked Arrow Hit

1. Start with player room `1`, Wumpus room `11`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Enter `s 2 10 11`.
3. Verify the output includes `AHA! YOU GOT THE WUMPUS!`.
4. Verify the game is won.

## QA-3: Invalid Segment Deviates Randomly

1. Start with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Configure invalid arrow movement to choose room `5`.
3. Configure the Wumpus wake outcome as `stay`.
4. Enter `s 3 4`.
5. Verify the arrow follows the observable path `5, 4` if the UI exposes arrow tracing, or verify equivalent deterministic diagnostics in setup-inspection mode.
6. Verify arrows remaining is `4`.
7. Verify the game remains in progress.

## QA-4: Deviation Can Hit The Wumpus

1. Start with player room `1`, Wumpus room `5`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Configure invalid arrow movement to choose room `5`.
3. Enter `s 3`.
4. Verify the output includes `AHA! YOU GOT THE WUMPUS!`.
5. Verify the game is won.

## QA-5: Arrow Self-Hit

1. Start with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Enter `s 2 1`.
3. Verify the output includes `OOPS! ARROW GOT YOU!`.
4. Verify the game is lost.

## QA-6: Miss Wakes Wumpus But Player Survives

1. Start with player room `1`, Wumpus room `10`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Configure the Wumpus wake outcome as `stay`.
3. Enter `s 5`.
4. Verify arrows remaining is `4`.
5. Verify the game remains in progress.

## QA-7: Miss Wakes Wumpus Onto Player

1. Start with player room `1`, Wumpus room `10`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Configure the Wumpus wake outcome as `move to 1`.
3. Enter `s 5`.
4. Verify arrows remaining is `4`.
5. Verify the game is lost because the Wumpus moved onto the player.

## QA-8: Last Arrow Miss Loses

1. Start with player room `1`, Wumpus room `10`, pit rooms `14, 15`, bat rooms `16, 17`, and `1` arrow.
2. Configure the Wumpus wake outcome as `stay`.
3. Enter `s 5`.
4. Verify arrows remaining is `0`.
5. Verify the output includes `YOU RAN OUT OF ARROWS`.
6. Verify the game is lost.

## QA-9: Invalid Shot Syntax Does Not Advance State

1. Start with player room `1`, Wumpus room `10`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Enter `s`.
3. Verify the output includes `CAN'T SHOOT THERE`.
4. Verify arrows remaining is still `5`.
5. Enter `s 2 10 11 12 13 14`.
6. Verify the output includes `CAN'T SHOOT THERE`.
7. Verify arrows remaining is still `5`.
