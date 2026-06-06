# Shell Random Event Choices End-to-End QA Suite

## Scope

Verify that unforced random event choices vary when the game is played from the `htw` shell command. QA must drive `htw` through shell execution, terminal input, and documented user-facing observation output only.

## Required UI Affordances

- `htw` is available from the project command directory or documented installation path.
- `htw` accepts documented fixed setup options for player, Wumpus, pit, and bat rooms.
- `htw` accepts a documented seed option so QA can replay a shell-launched game deterministically.
- `htw` exposes enough documented observation output for QA to record bat transport rooms, Wumpus wake rooms, and arrow fallback rooms without calling project APIs.
- Omitting `--bat-transport`, `--wumpus-wake`, or `--arrow-deviation` means the corresponding event is chosen by the shell-launched game's random source.
- Supplying `--bat-transport`, `--wumpus-wake`, or `--arrow-deviation` remains a deterministic QA override and is not required for normal `htw` play.

## QA-1: Unforced Bat Transport Varies From `htw`

1. Put the documented project command directory on `PATH`.
2. For each seed in `51, 52, 53, 54, 55, 56, 57, 58`, run `htw` with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `2, 17`, that seed, and no bat transport override.
3. Enter `n`, then `m 2`.
4. Record the observed bat transport destination from documented terminal or observation output.
5. Verify every recorded destination is a cave room other than bat rooms `2` and `17`.
6. Verify at least two distinct destinations are observed.
7. Verify the destination is not always room `1`.

## QA-2: Unforced Wumpus Wake Varies From `htw`

1. For each seed in `101, 102, 103, 104, 105, 106, 107, 108`, run `htw` with player room `1`, Wumpus room `2`, pit rooms `14, 15`, bat rooms `16, 17`, that seed, and no Wumpus wake override.
2. Enter `n`, then `m 2`.
3. Record the observed Wumpus wake destination from documented terminal or observation output.
4. Verify every recorded destination is one of `2, 1, 3, 10`.
5. Verify at least two distinct destinations are observed.
6. Verify the destination is not always room `2`.

## QA-3: Unforced Arrow Fallback Varies From `htw`

1. For each seed in `201, 202, 203, 204, 205, 206, 207, 208`, run `htw` with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`, that seed, and no arrow deviation override.
2. Enter `n`, then `s 20`.
3. Record the first observed arrow room from documented terminal or observation output.
4. Verify every recorded first arrow room is one of room `1`'s exits: `2, 5, 8`.
5. Verify at least two distinct first arrow rooms are observed.
6. Verify the first arrow room is not always room `2`.

## QA-4: Deterministic Overrides Still Override Randomness

1. Run `htw` with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `2, 17`, and `--bat-transport 10`.
2. Enter `n`, then `m 2`.
3. Verify the observed bat transport destination is room `10`.
4. Run `htw` with player room `1`, Wumpus room `2`, pit rooms `14, 15`, bat rooms `16, 17`, and `--wumpus-wake 3`.
5. Enter `n`, then `m 2`.
6. Verify the observed Wumpus wake destination is room `3`.
7. Run `htw` with player room `1`, Wumpus room `13`, pit rooms `14, 15`, bat rooms `16, 17`, and `--arrow-deviation 5`.
8. Enter `n`, then `s 20`.
9. Verify the first observed arrow room is room `5`.
