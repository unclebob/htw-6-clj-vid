# Hunt the Wumpus - Implementation Tasks

The game must be a **faithful recreation** of the 1973 original as detailed in [Hunt_the_Wumpus_Features.md](./Hunt_the_Wumpus_Features.md). This includes the dodecahedral topology, the full crooked-arrow mechanic (1-5 rooms with explicit path + random deviation), exact hazard counts and behaviors, warning messages, Wumpus wake/move probabilities, and all flavor text.

This plan also incorporates deliberate extensions not present in the 1973/1976 original: the Holy Hand Grenade (HHG) — see Task 5 — , the Sleepy Wumpus variant — see Task 6 — , the Jumping Wumpus — see Task 7, and a Rest command — see Task 8. All such behavior must still be specified and implemented using the same rigorous natural-language scenario + custom-runner + three-laws-of-TDD process.

---

## Task 1: Project Scaffolding, Cave Graph, and Entity Placement

**Goal:** Set up the project and implement the fixed cave topology plus deterministic/random placement of game elements.

**Key Deliverables:**
- Clean directory layout (e.g. `src/htw/`, `tests/`, `features/` for scenarios, and appropriate build/packaging configuration).
- The canonical 20-room dodecahedral adjacency list (bidirectional, exactly 3 exits per room). Source from historical BASIC listings or verified faithful recreations.
- Core domain types: `Cave`, `Room` (numbered 1-20), `Player`, `Wumpus`, `Pit`, `Bat`.
- Placement logic that puts player + 1 Wumpus + 2 pits + 2 bats into **distinct** rooms.
- Support for reproducible "SAME SET UP" via seeding or explicit configuration.
- Pure query functions: `neighbors(room)`, `hazards_adjacent_to(room)`, etc.

**BDD/TDD Process (mandatory):**
1. Create scenario files with natural-language Given/When/Then for:
   - Map connectivity invariants (every room has exactly 3 tunnels, graph is connected, no self-loops).
   - Placement rules (exactly 5 distinct occupied rooms, valid room numbers).
   - Reproducible setup (same seed → identical placement).
2. Build the custom scenario parser/runner.
3. Run scenarios → observe failures.
4. Implement minimal code to pass.
5. Refactor. Repeat for each new behavior.
6. Run coverage; do not proceed until ≥90% on this module.

**Success Criteria:**
- All Task 1 scenarios pass.
- Linter clean.
- Coverage report in the high 90s.
- Game state can be instantiated and inspected in tests without side effects.

---

## Task 2: Movement, Warnings, and Basic Hazard Resolution

**Goal:** Implement player movement, hazard resolution on arrival, adjacency warnings, and Wumpus wake/move behavior triggered by entry.

**Key Deliverables:**
- `move(to_room)` action that validates the tunnel, relocates the player, then immediately resolves hazards in priority order:
  - Bottomless pit → instant loss ("YYYIIIIEEEE . . . FELL IN PIT").
  - Super bat → snatch + random relocation (may land on pit or Wumpus).
  - Wumpus room → Wumpus wakes and may move; if now co-located, eats player.
- Warning generation at the start of each turn for hazards in the **three adjacent rooms only**:
  - Wumpus: `I SMELL A WUMPUS`
  - Bats: `BATS NEARBY`
  - Pit: `I FEEL A DRAFT`
  (Multiple warnings can appear; order per original spec.)
- Wumpus wake-and-move logic (0.75 probability move to one of 3 neighbors, 0.25 stay — or equivalent uniform choice among 4 options). Wumpus is immune to pits and bats.

**BDD/TDD Process (mandatory):**
- Write G/W/T scenarios (ignorant of code) covering:
  - Safe move to empty room.
  - Move into each hazard type (and combinations via bat transport).
  - All warning combinations.
  - Wumpus movement on entry (seeded for determinism).
  - Bat transport into danger.
- Every scenario must be executed and fail before any glue or production code is written for it.
- Use custom runner; no `@given` or framework magic that bypasses the spirit.
- Follow the three laws for every micro-behavior.

**Success Criteria:**
- Faithful message text and hazard precedence.
- Warnings only for immediate adjacency.
- Wumpus behavior matches spec exactly (testable via injection of RNG).
- Branch coverage high 90s for movement/hazard paths.
- No movement or hazard logic leaks into Task 1 code.

---

## Task 3: Crooked Arrow Shooting

**Goal:** Deliver the game's signature "crooked arrow" mechanic with full path specification, random deviation on invalid segments, self-hit detection, and post-miss Wumpus reaction.

**Key Deliverables:**
- `shoot(path: list[Room])` or interactive equivalent:
  - First choose number of rooms (1–5).
  - Then supply the exact sequence of target rooms.
- Arrow traversal:
  - Follow the supplied rooms in order when tunnels exist.
  - On any invalid tunnel from the arrow's current position: arrow "moves at random to the next room".
- Outcomes:
  - Arrow room sequence contains the Wumpus → immediate win ("AHA! YOU GOT THE WUMPUS!").
  - Arrow path contains the player's current room at any point → player shoots self and loses.
  - Otherwise: arrow spent, Wumpus wakes and performs its move, then check for post-move co-location (eat).
- Exactly 5 arrows at start. Exhaustion = loss.

**BDD/TDD Process (mandatory):**
- Scenarios for:
  - Straightforward hits (various lengths).
  - Paths with one or more invalid segments (verify random choice occurs).
  - Self-hit at step 1, step 3, etc.
  - Miss that causes Wumpus to move onto player.
  - Arrow count decrement and exhaustion loss.
  - Seeded randomness for the deviation cases.
- All scenarios written, run, and failing visibly **before** any shooting code or step defs exist.
- Custom glue only; real production calls.

**Success Criteria:**
- The full crooked-arrow rules (not the common simplification of "shoot one adjacent room").
- Random deviation is observable and tested.
- Every described loss condition for arrows covered.
- Coverage high 90s.
- Domain remains decoupled from I/O.

---

## Task 4: Interactive Game Loop, Full I/O, Win/Lose, and Replay

**Goal:** Assemble all prior pieces into a complete, playable, text-only game that uses a compact single-line command format for player actions (`m <room>`, `s <room1> ...`) while preserving the original 1975/1976 event messages, warnings, win/lose taunts, and flavor text. Add the "SAME SET UP" replay feature.

**Key Deliverables:**
- Main loop that, each turn, prints:
  - Current room.
  - The three tunnels.
  - Any warnings.
  - Arrows remaining (in versions that show it).
- Prompt: `SHOOT OR MOVE (S-M)?`
- The player responds with a single command line (case-insensitive):
  - `m <room>` — move to an adjacent room.
  - `s <room1> [<room2> ... <roomN>]` (1 to 5 rooms) — shoot a crooked arrow along the specified path.
- The I/O layer is responsible for parsing the command line, performing basic validation (syntax, number of rooms for shoot, adjacency for move, and valid arrow path rooms), calling the corresponding domain action (move or shoot), and reprompting cleanly on invalid input without advancing the game state.
- Invalid commands or impossible commands must report a clear error, such as `X IS NOT A COMMAND`, `CAN'T MOVE THERE`, or `CAN'T SHOOT THERE`, before reprompting.
- Exact original event messages (see features doc for samples).
- Win: "AHA! YOU GOT THE WUMPUS! HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!"
- Loss taunts + "HA HA HA - YOU LOSE!"
- On loss: `SAME SET UP (Y-N)?`
- Optional initial `INSTRUCTIONS (Y-N)?` block.
- Clean separation: domain logic has zero `print`/`input`; I/O layer only.
- End-to-end acceptance scenarios that drive the full loop (via test doubles for I/O or subprocess).

**BDD/TDD Process (mandatory):**
- High-level scenarios describing complete player experiences (win in N moves, various deaths, replay identical map, etc.), written using the compact single-line command format (`m 5`, `s 3 4 5`, etc.).
- Scenarios must also cover invalid commands, invalid move targets, invalid arrow paths, exact error text, reprompting behavior, and case-insensitivity.
- These scenarios must fail before the loop or I/O glue is written.
- Lower-level scenarios from Tasks 1-3 continue to protect behavior.
- Refactor aggressively after each passing increment to keep functions small and responsibilities clear.

**Success Criteria:**
- A human can play a full game via the terminal using the compact single-line command format and experience the original flavor through event messages, warnings, and taunts.
- All original event messages and flavor text from the features document are reproducible (command format itself follows the new `m`/`s` style rather than the 1970s multi-prompt dialogue).
- All core win/lose paths (including bat-to-Wumpus, arrow self-hit, Wumpus move after miss, arrow exhaustion) are possible and tested.
- Overall line + branch coverage in the high 90s.
- Linter clean.
- Coverage checked before any commit.
- The game is fun and faithful to the core rules and text.

---

## Task 5: Holy Hand Grenade (HHG)

**Goal:** Add the Holy Hand Grenade as a new collectible item and powerful delayed area-effect weapon. The feature must be fully specified, tested, and implemented using the project's mandatory BDD/TDD workflow (natural-language scenarios first, custom parser/runner, every scenario seen to fail, three laws of TDD, high coverage, small functions).

**Key Deliverables:**
- Startup placement (extends Task 1): Exactly one HHG placed randomly in a room that is **not** the player's room, **not** the Wumpus room, and **not** any pit or bat room. (15 possible rooms.)
- Acquisition: Player entering the HHG room automatically picks it up; the HHG is removed from the cave; player now carries it. The player is told exactly: `YOU FOUND THE HOLY HAND GRENADE! USE IT WISELY!` No adjacency warning for an unclaimed HHG (per supplied spec).
- New action available only when carrying the HHG: Throw.
  - The turn prompt must support the new choice (e.g. `SHOOT, MOVE OR THROW (S-M-T)?` when armed).
  - Player enters `t <room>` (case-insensitive) to throw the grenade into an adjacent room.
  - The throw action consumes the turn.
  - On throw: the player hears `YOU HEAR TIC...TIC...` (exact text and timing to be nailed down by scenarios). The grenade is now in flight toward the chosen room with a one-turn fuse.
- Delayed detonation: At the **end of the player's very next turn** (after whatever move or shoot the player performs next), the HHG detonates with `YOU HEAR A HORRENDOUS EXPLOSION!`.
  - Affects the target room + all rooms adjacent to the target (the thrown-to cave and its three neighbors).
  - "Destroys the contents":
    - Wumpus in any affected room → Wumpus killed → immediate win (appropriate victory message).
    - Bats in any affected room → bats destroyed/removed.
    - Pits unaffected (bottomless).
    - Player in the blast radius at detonation time → player dies (new loss condition and flavor text).
  - HHG is consumed on detonation.
- All state (carrying HHG, pending detonation room + timer) must be queryable via the pure testing API with no I/O side effects.
- New win and loss paths integrated into the overall game loop and end-to-end scenarios.
- A simple documented console command runs the game from a clean checkout (for example `make run`, `python -m htw`, or the project-standard equivalent). The command must require no IDE or manual module wiring.
- Exact flavor text for acquisition (`YOU FOUND THE HOLY HAND GRENADE! USE IT WISELY!`), the `YOU HEAR TIC...TIC...` cue, the `YOU HEAR A HORRENDOUS EXPLOSION!` announcement, and any new death message (e.g. `YOU ARE BLOWN UP BY YOUR OWN HOLY HAND GRENADE!`) must be specified in scenarios and matched exactly in code.
- The feature must not break any existing behavior when the HHG is absent or not thrown.

**BDD/TDD Process (mandatory):**
- Write natural-language Given/When/Then scenarios (completely ignorant of code) covering at minimum:
  - Reproducible placement of the HHG in a valid empty room (seed determinism).
  - Entering the HHG room acquires it and the player is told exactly `YOU FOUND THE HOLY HAND GRENADE! USE IT WISELY!`; subsequent visits to that room show no HHG.
  - Player without HHG cannot throw; invalid throw targets are rejected with exact error text such as `CAN'T THROW THERE`.
  - Invalid commands and impossible commands report exact errors before reprompting, including unknown commands like `x` (`X IS NOT A COMMAND`), invalid moves (`CAN'T MOVE THERE`), invalid arrow paths (`CAN'T SHOOT THERE`), and invalid grenade throws (`CAN'T THROW THERE`).
  - Successful throw into an adjacent room prints the `TIC...TIC...` cue and consumes the turn; no immediate destruction.
  - After a throw, the *next* player turn (any legal move or shot) completes normally, then the explosion occurs at end-of-turn.
  - Explosion in target + neighbors: Wumpus death (win) from any of the four rooms (with `YOU HEAR A HORRENDOUS EXPLOSION!`).
  - Explosion destroys bats in radius but leaves pits intact.
  - Player caught in the blast (by remaining in place or moving into the radius before detonation) produces loss.
  - Edge cases: Wumpus moves into the future blast zone between throw and boom; player throws then immediately moves safely out of the eventual radius; bat transport after throw but before explosion, etc.
  - All new messages (including the exact acquisition message `YOU FOUND THE HOLY HAND GRENADE! USE IT WISELY!`, `YOU HEAR TIC...TIC...`, and `YOU HEAR A HORRENDOUS EXPLOSION!`) and the changed prompt + single-line `t <room>` command when armed.
  - "SAME SET UP" replay preserves HHG placement and any in-flight state if applicable.
- Every single one of these scenarios must be created in feature files, executed via the custom runner, and observed to fail (with a clear, useful error) **before** any production code, step definitions, or glue for the HHG is written.
- Implement using the three laws of TDD for each micro-behavior.
- Because HHG placement touches Task 1, acquisition and throw affect movement/action resolution (Task 2/4), and delayed effects change the turn loop (Task 4), add the necessary new scenarios to the affected modules and keep the entire suite green.
- Refactor after each passing increment to keep functions small (cyclomatic complexity ≤ 5) and responsibilities clear.
- Run full coverage after the feature is integrated; do not consider the task complete until high 90s line and branch on the new and modified code.

**Success Criteria:**
- The complete HHG life cycle (place → acquire → throw → tic-tic → next-turn horrendous explosion) is playable and matches the supplied description.
- A single documented console command starts a playable game from the terminal.
- Both new victory condition (grenade kills Wumpus) and new defeat condition (player in blast) are reachable and fully scenario-covered.
- Invalid command, move, arrow-path, and throw inputs produce exact tested errors and do not advance game state.
- Zero regression: every scenario from Tasks 1–4 continues to pass exactly as before.
- All flavor text (including the exact acquisition message `YOU FOUND THE HOLY HAND GRENADE! USE IT WISELY!`, `YOU HEAR TIC...TIC...`, `YOU HEAR A HORRENDOUS EXPLOSION!`, and any player death by own grenade e.g. `YOU ARE BLOWN UP BY YOUR OWN HOLY HAND GRENADE!`) is exact and tested.
- Domain logic remains completely free of print/input; I/O layer only.
- Linter clean.
- Coverage in the high 90s (report checked before any commit).
- The added complexity does not inflate existing functions; new code follows the small-function guideline.
- Git discipline followed throughout.

---

## Task 6: Sleepy Wumpus

**Goal:** Extend the Wumpus with a probabilistic "sleepy" state that creates moments of stealthy approach, risky observation of the creature, and new death/flavor text while preserving every aspect of the original 1973 Wumpus behavior when it is awake.

**Key Deliverables:**
- Wumpus gains an internal asleep/awake state (default awake; transitions driven by the rules below and integrated with existing wake triggers such as arrows).
- When the player enters a room adjacent to the Wumpus:
  - 20% chance the Wumpus is asleep → player hears `YOU HEAR HORRIBLE SNORING` **in addition to** the normal `I SMELL A WUMPUS`.
  - Otherwise (80%) normal awake behavior and warning.
- If the player moves from an adjacent room to a non-adjacent room while the Wumpus was asleep:
  - The Wumpus awakens with a loud `YOU HEAR A SNORT AND "HUH?"`.
- When the player enters the Wumpus's own room while it is sleeping:
  - 10% chance the Wumpus awakens and kills the player (`YOU HEAR THE WUMPUS SAY "YUMMY BREAKFAST!"`), game over (new loss condition).
  - 90% chance the player sees `YOU SEE THE HUDDLED HORRIBLE SHAPE OF THE SLEEPING WUMPUS` (no death; player survives the turn in the room).
- When the player leaves the Wumpus room after having seen the sleeping shape:
  - The player hears `YOU HEAR A PETULANT SCREAM!` (Wumpus awakens and is now alert).
- All random choices (20% / 10%) must be driven by injectable RNG so they are fully deterministic and reproducible under a given seed.
- New messages and the conditional extra snoring must appear exactly as specified; normal hazard precedence and Wumpus post-miss movement remain unchanged.
- The sleepy state and all new transitions must be observable and controllable via the pure testing API (no I/O).

**BDD/TDD Process (mandatory):**
- Write natural-language Given/When/Then scenarios (ignorant of implementation) covering:
  - Entering an adjacent room yields the normal Wumpus smell plus `YOU HEAR HORRIBLE SNORING` on 20% of seeded trials.
  - Moving away from a sleeping Wumpus (leaving adjacency without entering its room) triggers `YOU HEAR A SNORT AND "HUH?"` awakening.
  - Entering the sleeping Wumpus room: 10% immediate `YOU HEAR THE WUMPUS SAY "YUMMY BREAKFAST!"` death vs. 90% `YOU SEE THE HUDDLED HORRIBLE SHAPE OF THE SLEEPING WUMPUS`.
  - Leaving the Wumpus room after seeing it asleep produces `YOU HEAR A PETULANT SCREAM!` and awakens it.
  - Integration with existing mechanics: shooting an arrow still wakes the Wumpus; normal awake entry still kills; post-miss Wumpus move still occurs, etc.
  - Reproducible sleepy observations via "SAME SET UP" / seeding.
  - Edge cases: Wumpus already awake (no snoring), multiple visits, bat transport into adjacency, etc.
- Every scenario must be written, added to the feature files, run through the custom parser/runner, and visibly fail **before** any production code or step definitions for sleepy behavior are written.
- Extend and protect the movement/hazard resolution scenarios from Task 2 and the I/O loop scenarios from Task 4.
- Follow the three laws of TDD for each increment. Refactor to keep functions small.
- Because this changes Wumpus state, warnings, and entry resolution, new scenarios must be added to the affected modules and the entire existing suite must stay green.

**Success Criteria:**
- The full sleepy Wumpus life cycle (adjacent snoring observation, retreat wake, risky entry with breakfast risk vs. safe-ish peek, petulant scream on exit) is playable and matches the supplied description exactly.
- Both the new `YOU HEAR THE WUMPUS SAY "YUMMY BREAKFAST!"` loss and the new observation path are reachable and covered by scenarios.
- 100% regression-free: every scenario from Tasks 1–5 (including all original Wumpus behavior) continues to pass when the Wumpus is awake.
- All 20%/10% probabilities and new flavor text (`YOU HEAR HORRIBLE SNORING`, `YOU HEAR A SNORT AND "HUH?"`, `YOU HEAR THE WUMPUS SAY "YUMMY BREAKFAST!"`, `YOU SEE THE HUDDLED HORRIBLE SHAPE OF THE SLEEPING WUMPUS`, `YOU HEAR A PETULANT SCREAM!`) are exact and deterministically testable.
- Domain remains pure; I/O only in the loop layer.
- Linter clean.
- Line + branch coverage in the high 90s on the modified Wumpus and hazard code (checked before commit).
- Functions stay small; cyclomatic complexity ≤ 5 where practical.
- Git discipline observed (coverage before any commit).

---

## Task 7: Jumping Wumpus

**Goal:** Add a new spontaneous Wumpus behavior in which, on any given turn, there is a small chance the Wumpus performs two successive jumps along the cave graph. This introduces new auditory cues, a high-lethality trample on the first landing, a non-fatal "slam" encounter, and a second-landing sighting that grants the player an escape turn — all while preserving the original game and the prior two extensions.

**Key Deliverables:**
- Each turn (integrated into the main game loop from Task 4): 5% chance the Wumpus will make **two jumps** through adjacent caves (following the dodecahedral topology).
- When the jumps occur: the player hears `YOU HEAR WHUMP, WHUMP.`
- If the **first jump** lands in the player's current room:
  - 50% chance: the Wumpus tramples the player to death (new loss condition, e.g. `THE WUMPUS TRAMPLES YOU TO DEATH!`).
  - Otherwise: the player is `YOU ARE SLAMMED AGAINST THE CAVE WALL BY THE SNARLING WUMPUS!` (non-fatal; player survives the encounter and the turn continues or ends per scenario definition).
- If the Wumpus lands in the player's cave on the **second jump**:
  - The player sees `YOU SEE THE BLOODSTAINED EYES OF THE WUMPUS APPRAISING YOU!`.
  - The player is **not** killed and is explicitly able to take their normal turn to escape (no automatic death on second-jump landing).
- All jumps respect the cave adjacency rules (no teleporting; only legal tunnels).
- Both probabilities (5% for the double-jump event, 50% for trample on first landing) must be driven by injectable/testable RNG so they are fully deterministic under a seed.
- The jumping behavior must coexist with (and not break) normal Wumpus wake/move logic, Sleepy Wumpus, Holy Hand Grenade detonation, arrow mechanics, etc.
- New messages and the conditional trample/slam/eyes outcomes must be exact and fully scenario-driven.
- All new state/transitions observable via the pure testing API with no I/O.

**BDD/TDD Process (mandatory):**
- Write natural-language Given/When/Then scenarios (completely ignorant of code) covering at minimum:
  - On any turn, 5% chance (seeded) triggers the double-jump event and prints `YOU HEAR WHUMP, WHUMP.`
  - First jump lands on player: 50% immediate trample death vs. `YOU ARE SLAMMED AGAINST THE CAVE WALL BY THE SNARLING WUMPUS!` (non-fatal).
  - Second jump lands on player: `YOU SEE THE BLOODSTAINED EYES OF THE WUMPUS APPRAISING YOU!` observed, and the player gets a full turn to escape (no death).
  - Jumps only move to legal adjacent rooms.
  - The event can occur independently of player movement or other actions.
  - Interactions and non-interference with other mechanics: sleepy state, HHG in flight, post-arrow Wumpus move, bat transport, "SAME SET UP" reproducibility, etc.
  - Exact message timing and precedence (e.g. when combined with other warnings or events in the same turn).
- Every scenario must be created, executed via the custom parser/runner, and observed to fail visibly **before** any production code or glue for the jumping behavior is written.
- Extend the turn-processing logic (primarily Task 4) and any Wumpus movement primitives (Task 2) as needed; add protecting scenarios to those modules.
- Follow the three laws of TDD for each micro-increment. Refactor aggressively to keep functions small.
- Because this is a new random event in the main loop, all prior end-to-end scenarios from Tasks 4–6 must remain green.

**Success Criteria:**
- The complete jumping life cycle (5% double-jump trigger, "Whump, whump.", first-jump trample death vs. slam, second-jump "bloodstained eyes" sighting + guaranteed escape turn) is playable and matches the supplied description exactly.
- Both the new trample death and the non-lethal second-jump sighting paths are reachable and covered.
- 100% regression-free: every scenario from Tasks 1–6 (including normal Wumpus behavior, Sleepy Wumpus, and HHG) continues to pass.
- All 5%/50% probabilities and new flavor text (`YOU HEAR WHUMP, WHUMP.`, trample death, `YOU ARE SLAMMED AGAINST THE CAVE WALL BY THE SNARLING WUMPUS!`, `YOU SEE THE BLOODSTAINED EYES OF THE WUMPUS APPRAISING YOU!`) are exact and deterministically testable via seeded RNG.
- Domain logic remains completely free of print/input.
- Linter clean.
- Line + branch coverage in the high 90s on the new jumping logic and affected turn processing (coverage report checked before any commit).
- Functions remain small (cyclomatic complexity ≤ 5 where practical).
- Git discipline followed (coverage before commit, ask before push).

---

## Task 8: Rest Command

**Goal:** Add a deliberate rest action that consumes a full player turn without moving, shooting, or throwing, while still allowing every normal turn-level operation and pending effect to occur.

**Key Deliverables:**
- New player command:
  - `r` or `rest` (case-insensitive) — rest for one full turn.
- Rest does **not** change the player's room.
- Rest does **not** spend an arrow.
- Rest does **not** throw or otherwise affect the Holy Hand Grenade unless one is already in flight.
- Rest consumes a complete turn:
  - The usual start-of-turn room display, tunnels, warnings, prompt, and command parsing still occur.
  - Any spontaneous turn events, including Jumping Wumpus checks, still occur according to their normal timing.
  - Any pending end-of-turn effects, including Holy Hand Grenade detonation, still occur after the rest action.
  - Existing Wumpus state transitions that are tied to passage of a turn rather than movement or shooting still occur.
- Rest is a domain action with no I/O side effects; the I/O layer only parses `r` / `rest` and reports messages.
- Invalid forms such as `r 5`, `rest 12`, or unrelated input are rejected without advancing game state.

**BDD/TDD Process (mandatory):**
- Write natural-language Given/When/Then scenarios (completely ignorant of code) covering at minimum:
  - Rest leaves the player in the same room and does not decrement arrows.
  - Rest consumes a full turn and advances the turn counter or equivalent observable turn state.
  - Rest preserves ordinary warning display before the command.
  - Rest can trigger turn-level Jumping Wumpus behavior exactly as any other turn can.
  - Rest after throwing the Holy Hand Grenade causes the pending grenade to detonate at the normal end-of-next-turn timing.
  - Rest while safely outside an HHG blast radius survives; rest while inside the eventual blast radius loses with the exact grenade death message.
  - Rest does not perform movement hazard resolution unless another turn-level effect changes positions or detonates.
  - Invalid rest command syntax reprompts cleanly and does not advance the game.
  - `r`, `R`, `rest`, and mixed-case variants are accepted.
- Every scenario must be created, executed via the custom parser/runner, and observed to fail visibly **before** any production code or glue for the rest command is written.
- Extend the full-loop scenarios from Task 4 and the turn-processing scenarios from Tasks 5–7 as needed.
- Follow the three laws of TDD for each micro-increment. Refactor to keep the rest action small and to avoid special-case logic leaking into unrelated actions.
- Because rest exercises the turn pipeline without a movement/shooting payload, all prior scenarios from Tasks 1–7 must remain green.

**Success Criteria:**
- A human player can enter `r` or `rest` to intentionally spend a turn in place.
- Rest changes no player position, arrow count, or HHG carried state by itself.
- All normal turn operations still occur, including warnings, random turn events, sleepy/jumping Wumpus behavior where applicable, and delayed HHG detonation.
- Exact command parsing and invalid-input behavior are scenario-covered.
- 100% regression-free: every scenario from Tasks 1–7 continues to pass.
- Domain logic remains completely free of print/input.
- Linter clean.
- Line + branch coverage remains in the high 90s on affected turn-processing and command-parsing code.
- Functions remain small (cyclomatic complexity ≤ 5 where practical).
- Git discipline followed (coverage before commit, ask before push).
