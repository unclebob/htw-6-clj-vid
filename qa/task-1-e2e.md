# Task 1 End-to-End QA Suite

## Scope

Verify the visible setup and inspection affordances for the initial Hunt the Wumpus implementation through the user interface only. QA must not call project APIs, constructors, namespaces, or private functions.

## Required UI Affordances

- A documented command starts the program from a clean checkout.
- A documented setup-inspection mode is exposed as a user-facing command-line affordance.
- The setup-inspection mode accepts a seed or same-setup replay input.
- The setup-inspection mode prints the cave exits, player room, Wumpus room, pit rooms, and bat rooms in a deterministic text format.

## QA-1: Cave Topology Is Visible And Canonical

1. Start the program in setup-inspection mode.
2. Inspect the printed room exits.
3. Verify rooms 1 through 20 are listed exactly once.
4. Verify each room lists exactly three exits.
5. Verify every listed exit is a room number from 1 through 20.
6. Verify no room lists itself as an exit.
7. Verify every tunnel is bidirectional.
8. Verify the printed exits match the canonical dodecahedral topology:

```text
1: 2, 5, 8
2: 1, 3, 10
3: 2, 4, 12
4: 3, 5, 14
5: 1, 4, 6
6: 5, 7, 15
7: 6, 8, 17
8: 1, 7, 9
9: 8, 10, 18
10: 2, 9, 11
11: 10, 12, 19
12: 3, 11, 13
13: 12, 14, 20
14: 4, 13, 15
15: 6, 14, 16
16: 15, 17, 20
17: 7, 16, 18
18: 9, 17, 19
19: 11, 18, 20
20: 13, 16, 19
```

## QA-2: Seeded Placement Is Valid

1. Start the program in setup-inspection mode with seed `1973`.
2. Verify the output identifies exactly one player room.
3. Verify the output identifies exactly one Wumpus room.
4. Verify the output identifies exactly two pit rooms.
5. Verify the output identifies exactly two bat rooms.
6. Verify all five hazard rooms are distinct.
7. Verify the player room is distinct from every hazard room.
8. Verify every printed placement room is numbered from 1 through 20.

## QA-3: Repeated Seed Is Reproducible

1. Start the program in setup-inspection mode with seed `1975`.
2. Record the player, Wumpus, pit, and bat rooms.
3. Start the program in setup-inspection mode with seed `1975` again.
4. Verify all recorded rooms are identical.

## QA-4: Different Seeds Are Independent Setups

1. Start the program in setup-inspection mode with seed `1973`.
2. Record the player, Wumpus, pit, and bat rooms.
3. Start the program in setup-inspection mode with seed `1976`.
4. Verify the second setup is valid using QA-2.
5. Verify QA does not require the two setups to be different; different seeds may collide by chance.

## QA-5: Same Setup Reuse Preserves Placement

1. Start the program in setup-inspection mode with seed `1973`.
2. Record the player, Wumpus, pit, and bat rooms.
3. Use the documented same-setup UI affordance to request reuse of that setup.
4. Inspect the reused setup through the documented UI affordance.
5. Verify the reused setup has the same player, Wumpus, pit, and bat rooms as the original setup.

## QA-6: Adjacent Hazard Inspection Matches Topology

1. Start the program in setup-inspection mode with a documented explicit setup or scripted setup input:
   - player room `1`
   - Wumpus room `2`
   - pit rooms `3, 4`
   - bat rooms `5, 6`
2. Ask the UI to inspect adjacent hazards for room `1`.
3. Verify the visible result reports one adjacent Wumpus, zero adjacent pits, and one adjacent bat.
4. Verify the output does not report hazards from non-adjacent rooms.
