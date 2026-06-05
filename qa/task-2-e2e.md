# Task 2 End-to-End QA Suite

## Scope

Verify movement, immediate hazard resolution, warnings, and Wumpus wake behavior through user-visible commands and output. QA must not call project APIs or inspect private state.

## Required UI Affordances

- A documented command starts the program from a clean checkout.
- A documented seeded or scripted setup mode lets QA choose initial player, Wumpus, pit, and bat rooms.
- A documented deterministic randomness mode lets QA choose bat transport and Wumpus wake outcomes.
- The visible turn display includes the current room, available tunnels, warning messages, and command prompt.

## QA-1: Safe Move

1. Start with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`.
2. Observe room `1` and tunnels `2, 5, 8`.
3. Enter `m 2`.
4. Verify the next visible state shows the player in room `2`.
5. Verify no loss, win, bat, pit, or Wumpus-eat message appears.

## QA-2: Invalid Move Does Not Advance State

1. Start with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`.
2. Enter `m 3`.
3. Verify the output includes `CAN'T MOVE THERE`.
4. Verify the next prompt still shows the player in room `1`.
5. Verify no hazard messages appear and the game remains in progress.

## QA-3: Pit Death

1. Start with player room `1`, Wumpus room `13`, pit rooms `2, 15`, bat rooms `16, 17`.
2. Enter `m 2`.
3. Verify the output includes `YYYIIIIEEEE . . . FELL IN PIT`.
4. Verify the game is lost.

## QA-4: Bat Transport To Safe Room

1. Start with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `2, 17`.
2. Configure the next bat transport destination as room `10`.
3. Enter `m 2`.
4. Verify the output includes `ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!`.
5. Verify the next visible state shows the player in room `10`.
6. Verify the game remains in progress.

## QA-5: Bat Transport To Pit

1. Start with player room `1`, Wumpus room `13`, pit rooms `10, 15`, bat rooms `2, 17`.
2. Configure the next bat transport destination as room `10`.
3. Enter `m 2`.
4. Verify the bat snatch message appears.
5. Verify the pit death message appears.
6. Verify the game is lost.

## QA-6: Bat Transport To Wumpus

1. Start with player room `1`, Wumpus room `10`, pit rooms `14, 15`, bat rooms `2, 17`.
2. Configure the next bat transport destination as room `10`.
3. Configure the Wumpus wake outcome as `stay`.
4. Enter `m 2`.
5. Verify the bat snatch message appears.
6. Verify the game is lost because the Wumpus remains with the player.

## QA-7: Warning Messages Are Adjacent Only

1. Start with player room `1`, Wumpus room `2`, pit rooms `5, 14`, bat rooms `8, 17`.
2. Inspect the start-of-turn output.
3. Verify the output includes `I SMELL A WUMPUS`, `BATS NEARBY`, and `I FEEL A DRAFT`.
4. Start with player room `1`, Wumpus room `6`, pit rooms `3, 4`, bat rooms `7, 9`.
5. Inspect the start-of-turn output.
6. Verify none of the three warning messages appears.

## QA-8: Wumpus Wake Move

1. Start with player room `1`, Wumpus room `2`, pit rooms `14, 15`, bat rooms `16, 17`.
2. Configure the Wumpus wake outcome as `move to 3`.
3. Enter `m 2`.
4. Verify the game remains in progress.
5. Verify subsequent visible inspection shows the Wumpus effect moved away from room `2`.

## QA-9: Wumpus Wake Stay

1. Start with player room `1`, Wumpus room `2`, pit rooms `14, 15`, bat rooms `16, 17`.
2. Configure the Wumpus wake outcome as `stay`.
3. Enter `m 2`.
4. Verify the game is lost because the Wumpus is with the player.
