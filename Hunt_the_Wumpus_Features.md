# Hunt the Wumpus

**A faithful description of the original 1973 text-based game by Gregory Yob**

## Overview

*Hunt the Wumpus* is a turn-based, text-only adventure game in which the player explores a dark cave system of 20 interconnected rooms, using limited sensory warnings to locate and slay a hidden monster called the Wumpus with "crooked arrows," while avoiding two deadly hazards: bottomless pits and super bats.

The game was created in early 1973 by Gregory Yob as a deliberate alternative to the grid-based hide-and-seek games (such as Hurkle, Mugwump, and Snark) popular at the time. Yob wanted a topological rather than Cartesian playfield. The caves are arranged as the vertices of a regular dodecahedron—each room has exactly three tunnels leading to other rooms. The layout is fixed and numbered so that dedicated players can (and were encouraged to) draw permanent maps.

The game was first shared via the People's Computer Company (PCC), published with source code and instructions in the October 1975 issue of *Creative Computing*, and republished in *The Best of Creative Computing, Volume 1* (1976). Tapes were sold by mail order.

## The Cave System

- **20 rooms**, numbered for reference (commonly 1–20 or 0–19 in surviving listings).
- **Topology**: Every room connects to exactly three others. The overall graph is that of the vertices of a dodecahedron (12 pentagonal faces). There is no "north," "up," or grid—only connectivity.
- The map is **fixed** across plays. Yob hoped players would create a "squashed dodecahedron" reference map. In practice, many players explored dynamically.
- At the beginning of a game the player, Wumpus, pits, and bats are placed randomly in distinct rooms. An option exists to restart with the *identical* placement ("SAME SET UP (Y-N)?").

## Hazards

### Bottomless Pits (exactly two rooms)
- Entering the room causes instant death: "YYYIIIIEEEE . . . FELL IN PIT" followed by "HA HA HA - YOU LOSE!"
- **Warning** (when in an adjacent room): `I FEEL A DRAFT`

### Super Bats (exactly two rooms)
- Entering a bat room triggers: a bat snatches the player and drops them in a completely random room ("ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!"). The new room may contain a pit or the Wumpus.
- Bats do **not** affect the Wumpus.
- **Warning** (adjacent room): `BATS NEARBY`

### The Wumpus (exactly one room)
- A large, sleepy creature with "sucker feet" (immune to pits) that is too heavy for bats to lift.
- **Warning** (when one room away): `I SMELL A WUMPUS`

The caves are completely dark; the player receives no visual information about adjacent rooms beyond the three numbered exits and the hazard warnings.

## Wumpus Behavior

The Wumpus is normally asleep.

It wakes in exactly two cases:

1. The player **enters its room**.
2. The player **shoots an arrow** anywhere in the cave system.

When it wakes, it makes one choice:

- Moves to one of the three neighboring rooms (probability 0.75).
- Stays where it is (probability 0.25).

(Implementation note from Yob's description: it effectively chooses uniformly among its current room + three neighbors—four possibilities.)

If, after this move, the Wumpus is in the same room as the player, it eats the player ("It eats you whole" / "HA HA HA - YOU LOSE!").

## Player Turn and Actions

At the start of each turn the player is told:

- Current room number.
- The three rooms reachable by tunnel.
- Any applicable warnings for hazards in those three adjacent rooms.
- Number of arrows remaining (in some versions).

The player then chooses **Move** or **Shoot**.

### Move
- The player names one of the three connected rooms.
- The player is transported there.
- Hazards (pit, bat, or Wumpus) are resolved immediately upon arrival.

### Shoot (Crooked Arrow)
- The player has exactly **five arrows** at the start of the game. Depleting them results in loss.
- The player first specifies the number of rooms (1 to 5) the arrow will travel.
- The player then names the sequence of rooms the arrow should traverse.
- The arrow follows the specified path:
  - It moves from room to room along the given numbers.
  - If the player names a room that has no direct tunnel from the arrow's current position, the arrow "moves at random to the next room" (selects a random valid exit from its present location).
- Outcomes:
  - Arrow enters the Wumpus's room → **Immediate win** ("AHA! YOU GOT THE WUMPUS!").
  - Arrow path passes through the player's own current room → player shoots self and loses.
  - Arrow completes its flight without hitting the Wumpus → miss. The Wumpus wakes and may move (see Wumpus Behavior). One arrow is spent. If the Wumpus then occupies the player's room, the player is eaten.
- This multi-room "crooked" path mechanic (the arrow can turn corners according to the player's directions) is one of the game's signature and most distinctive features.

## Warnings and Information

Warnings are issued only for hazards in rooms **immediately adjacent** (one tunnel away). The messages used in the original instructions are:

- Wumpus: `I SMELL A WUMPUS`
- Bats: `BATS NEARBY`
- Pit: `I FEEL A DRAFT`

Multiple warnings can be printed in one turn. The player must combine these clues with knowledge of the fixed map (or exploration) to triangulate the Wumpus's location.

No directional or distance information beyond adjacency is given.

## Winning and Losing Conditions

**Win**:
- Fire a crooked arrow that passes through the room containing the Wumpus.

**Lose**:
- Fall into a bottomless pit.
- Enter the Wumpus's room and it remains/eats you after waking.
- A missed arrow startles the Wumpus into moving onto your location.
- Get snatched by a bat into a pit or the Wumpus's room.
- Shoot yourself with your own arrow.
- Exhaust all five arrows without a successful hit.

After a loss the game typically displays a taunting message and offers the chance to replay the same cave configuration.

## Sample Interaction (verbatim excerpts from the 1976 publication)

```
INSTRUCTIONS (Y-N)?Y
WELCOME TO 'HUNT THE WUMPUS'

THE WUMPUS LIVES IN A CAVE OF 20 ROOMS: EACH ROOM HAS 3 TUNNELS LEADING TO OTHER
ROOMS. (LOOK AT A DODECAHEDRON TO SEE HOW THIS WORKS. IF YOU DON'T KNOW WHAT A
DODECAHEDRON IS, ASK SOMEONE)

***
HAZARDS:

BOTTOMLESS PITS - TWO ROOMS HAVE BOTTOMLESS PITS IN THEM
IF YOU GO THERE: YOU FALL INTO THE PIT (& LOSE!)

SUPER BATS  - TWO OTHER ROOMS HAVE SUPER BATS. IF YOU GO THERE, A BAT GRABS YOU
AND TAKES YOU TO SOME OTHER ROOM AT RANDOM. (WHICH MIGHT BE TROUBLESOME)

WUMPUS:

THE WUMPUS IS NOT BOTHERED BY THE HAZARDS (HE HAS SUCKER FEET AND IS TOO BIG FOR
A BAT TO LIFT). USUALLY HE IS ASLEEP. TWO THINGS WAKE HIM UP: YOUR ENTERING HIS
ROOM OR YOUR SHOOTING AN ARROW.

IF THE WUMPUS WAKES, HE MOVES (P=0.75) ONE ROOM OR STAYS STILL (P=0.25). AFTER
THAT, IF HE IS WHERE YOU ARE, HE EATS YOU UP (& YOU LOSE!)

YOU:

EACH TURN YOU MAY MOVE OR SHOOT A CROOKED ARROW 
MOVING: YOU CAN GO ONE ROOM (THRU ONE TUNNEL)
ARROWS: YOU HAVE 5 ARROWS. YOU LOSE WHEN YOU RUN OUT.

EACH ARROW CAN GO FROM 1 TO 5 ROOMS: YOU AIM BY TELLING THE COMPUTER THE ROOMS
YOU WANT THE ARROW TO GO TO. IF THE ARROW CAN'T GO THAT WAY (IE NO TUNNEL) IT
MOVES AT RANDOM TO THE NEXT ROOM.

IF THE ARROW HITS THE WUMPUS: YOU WIN.

IF THE ARROW HITS YOU: YOU LOSE.

WARNINGS:

WHEN YOU ARE ONE ROOM AWAY FROM WUMPUS OR HAZARD, THE COMPUTER SAYS:

WUMPUS - 'I SMELL A WUMPUS'

BAT - 'BATS NEARBY'

PIT - 'I FEEL A DRAFT'
```

Later sample play excerpt:

```
SHOOT OR MOVE (S-M)?M WHERE TO?15

I SMELL A WUMPUS! YOU ARE IN ROOM 15 TUNNELS LEAD TO 6 14 16

SHOOT OR MOVE (S-M)?S NO. OF ROOMS(1-5)?1 ROOM #?16
AHA! YOU GOT THE WUMPUS! HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!
```

## Design Philosophy and Notes from the Creator

In his own article, Gregory Yob described the genesis: annoyance at repeated grid-based games, desire for a topological alternative, choice of the dodecahedron because it was his favorite Platonic solid, invention of the "crooked arrow" that could turn corners, addition of pits then bats, and finally giving the Wumpus limited mobility to keep the game interesting.

He observed that while he imagined players would carefully map and approach the Wumpus methodically from multiple directions, most players adopted more direct hunting tactics.

The game was intentionally simple, hackable, and extensible—Yob himself quickly produced *Wumpus 2* (additional cave topologies) and described *Wumpus 3*.

## Faithful Implementation Notes

Any recreation claiming fidelity to the original should include:

- The fixed 20-room dodecahedral graph (standard adjacency lists appear in many preserved BASIC listings).
- Random but distinct placement of exactly one Wumpus, two pits, two bats, and the player.
- "Same setup" replay option.
- Full crooked-arrow mechanic: length 1–5 + explicit room path, with random deviation on invalid segments.
- Wumpus wake-and-move logic with the stated 0.75/0.25 probabilities (or equivalent 1-of-4 choice).
- Hazard immunity for the Wumpus.
- Exact warning phrases from the published instructions.
- Five arrows maximum.
- Loss on arrow exhaustion, self-hit, pit, bat transport to hazard, or Wumpus eating the player.

Many later ports and programming-task versions simplify the shooting to "fire into one adjacent room only." This changes the character of the game significantly and deviates from the 1973/1975 original.

## Sources

- Gregory Yob, "Hunt The Wumpus," *The Best of Creative Computing*, Vol. 1 (1976), pp. 247–250 (includes Yob's own development story and verbatim instructions + sample run).
- People's Computer Company newsletters (1973).
- Original BASIC source listings published alongside the game description (1975).
- Contemporary accounts and faithful recreations that preserve the crooked-arrow path specification and dodecahedral topology.

This document was compiled to remain as faithful as possible to the rules, flavor text, and mechanics as originally published and described by the game's creator.

---

*May your arrows remain straight.* — Gregory Yob