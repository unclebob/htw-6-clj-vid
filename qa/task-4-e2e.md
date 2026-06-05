# Task 4 End-to-End QA Suite

## Scope

Verify the complete text-only game loop through terminal input and terminal output. QA must drive the program as a user would, using command-line flags only when they are documented user-facing affordances for setup, seeding, or deterministic QA runs.

## Required UI Affordances

- A documented command starts a playable game from a clean checkout.
- The playable game accepts compact command lines:
  - `m <room>`
  - `s <room1> [<room2> ... <roomN>]`
- Commands are case-insensitive.
- A documented QA setup mode can choose initial rooms and deterministic random outcomes.
- Invalid input returns to the prompt without advancing game state.

## QA-1: Instructions Can Be Shown

1. Start the game.
2. At `INSTRUCTIONS (Y-N)?`, enter `y`.
3. Verify the output includes `WELCOME TO 'HUNT THE WUMPUS'`.
4. Verify the output describes pits, bats, the Wumpus, arrows, and warnings.
5. Verify the first turn is displayed after instructions.

## QA-2: Instructions Can Be Skipped

1. Start the game.
2. At `INSTRUCTIONS (Y-N)?`, enter `n`.
3. Verify the first turn is displayed.
4. Verify the full instruction block is not printed.

## QA-3: Turn Display

1. Start with player room `1`, Wumpus room `2`, pit rooms `5, 14`, bat rooms `8, 17`, and `5` arrows.
2. Verify the turn output includes `YOU ARE IN ROOM 1`.
3. Verify the turn output includes `TUNNELS LEAD TO 2 5 8`.
4. Verify the turn output includes `I SMELL A WUMPUS`, `BATS NEARBY`, and `I FEEL A DRAFT`.
5. Verify the turn output includes `ARROWS LEFT: 5`.
6. Verify the prompt is `SHOOT OR MOVE (S-M)?`.

## QA-4: Valid Move Advances The Loop

1. Start with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`.
2. Enter `m 2`.
3. Verify the next turn shows `YOU ARE IN ROOM 2`.
4. Verify the game remains in progress.

## QA-5: Valid Shot Wins

1. Start with player room `1`, Wumpus room `2`, pit rooms `14, 15`, bat rooms `16, 17`.
2. Enter `s 2`.
3. Verify the output includes `AHA! YOU GOT THE WUMPUS!`.
4. Verify the output includes `HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!`.
5. Verify the game ends as a win.

## QA-6: Invalid Commands Reprompt Without Advancement

1. Start with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Enter `x`.
3. Verify the output includes `X IS NOT A COMMAND`.
4. Verify the next prompt still shows room `1`.
5. Enter `m 3`.
6. Verify the output includes `CAN'T MOVE THERE`.
7. Verify the next prompt still shows room `1`.
8. Enter `s`.
9. Verify the output includes `CAN'T SHOOT THERE`.
10. Verify arrows remaining is still `5`.

## QA-7: Pit Loss

1. Start with player room `1`, Wumpus room `13`, pit rooms `2, 15`, bat rooms `16, 17`.
2. Enter `m 2`.
3. Verify the output includes `YYYIIIIEEEE . . . FELL IN PIT`.
4. Verify the output includes `HA HA HA - YOU LOSE!`.
5. Verify the output prompts `SAME SET UP (Y-N)?`.

## QA-8: Bat Transport Loss

1. Start with player room `1`, Wumpus room `13`, pit rooms `10, 15`, bat rooms `2, 17`.
2. Configure bat transport to room `10`.
3. Enter `m 2`.
4. Verify the output includes `ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!`.
5. Verify the output includes `YYYIIIIEEEE . . . FELL IN PIT`.
6. Verify the output includes `HA HA HA - YOU LOSE!`.

## QA-9: Arrow Self-Hit Loss

1. Start with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`.
2. Enter `s 2 1`.
3. Verify the output includes `OOPS! ARROW GOT YOU!`.
4. Verify the output includes `HA HA HA - YOU LOSE!`.

## QA-10: Wumpus Moves After Miss Onto Player

1. Start with player room `1`, Wumpus room `10`, pit rooms `14, 15`, bat rooms `16, 17`, and `5` arrows.
2. Configure Wumpus wake outcome as `move to 1`.
3. Enter `s 5`.
4. Verify arrows remaining is `4` if the terminal shows another state before death.
5. Verify the output includes `HA HA HA - YOU LOSE!`.

## QA-11: Arrow Exhaustion Loss

1. Start with player room `1`, Wumpus room `10`, pit rooms `14, 15`, bat rooms `16, 17`, and `1` arrow.
2. Configure Wumpus wake outcome as `stay`.
3. Enter `s 5`.
4. Verify the output includes `YOU RAN OUT OF ARROWS`.
5. Verify the output includes `HA HA HA - YOU LOSE!`.

## QA-12: Same Setup Replay

1. Start with player room `1`, Wumpus room `13`, pit rooms `2, 15`, bat rooms `16, 17`.
2. Enter `m 2`.
3. At `SAME SET UP (Y-N)?`, enter `y`.
4. Use the visible setup-inspection output or deterministic first-turn clues to verify the replay uses the same player, Wumpus, pit, and bat rooms.

## QA-13: New Setup Replay

1. Start with player room `1`, Wumpus room `13`, pit rooms `2, 15`, bat rooms `16, 17`.
2. Enter `m 2`.
3. At `SAME SET UP (Y-N)?`, enter `n`.
4. Verify a new valid game begins.
5. Do not require the new placement to differ from the old placement because random collisions are possible.
