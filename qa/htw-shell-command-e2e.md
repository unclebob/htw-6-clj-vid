# HTW Shell Command End-to-End QA Suite

## Scope

Verify that a user can start a normal playable game by typing `htw` at the shell. QA must drive the command through the shell and terminal input only.

## Required UI Affordances

- `htw` is a shell command available from the project command directory or documented installation path.
- `htw` starts a playable game without requiring flags, an IDE, a REPL, or manual classpath/module setup.
- A normal `htw` launch uses a fresh random seed.
- Any deterministic seed or setup options used for QA must be optional and must not change the behavior of plain `htw`.

## QA-1: Plain Command Starts The Game

1. Put the documented project command directory on `PATH`.
2. Run `htw`.
3. Verify the program starts without additional arguments.
4. Verify the output prompts `INSTRUCTIONS (Y-N)?`.
5. Enter `n`.
6. Verify the first turn is displayed.
7. Verify the prompt is `SHOOT OR MOVE (S-M)?`.

## QA-2: Plain Command Uses Fresh Random Seed

1. Put the documented project command directory on `PATH`.
2. Run `htw` in a mode that lets QA observe the startup seed or complete initial placement through a documented user-facing affordance.
3. Record the seed or complete initial placement.
4. Exit before taking a turn.
5. Run `htw` again in the same observation mode.
6. Record the second seed or complete initial placement.
7. Verify the two normal launches did not reuse the same seed.
8. Verify both launches produce valid placements.

## QA-3: Plain Command Is Not Deterministic By Default

1. Run `htw` twice without passing any seed or setup flags.
2. Verify neither command line contains an explicit seed.
3. Verify the second launch is not a same-setup replay of the first launch.
4. Verify both launches are playable games.

## QA-4: Optional QA Seeding Does Not Replace Plain Random Launch

1. Run the documented deterministic QA command with a fixed seed.
2. Verify repeated deterministic launches with that seed produce the same setup.
3. Run plain `htw`.
4. Verify plain `htw` still starts normally without requiring a seed.
5. Verify plain `htw` uses a fresh random seed rather than the previous QA seed.
