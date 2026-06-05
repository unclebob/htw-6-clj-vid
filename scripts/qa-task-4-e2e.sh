#!/usr/bin/env bash
set -euo pipefail

contains() {
  local haystack="$1"
  local needle="$2"
  if ! grep -Fq "$needle" <<<"$haystack"; then
    echo "missing expected output: $needle" >&2
    echo "$haystack" >&2
    exit 1
  fi
}

instructions="$(printf 'y\n' | bin/htw --player 1 --wumpus 13 --pits 14,15 --bats 16,17)"
contains "$instructions" "WELCOME TO 'HUNT THE WUMPUS'"
contains "$instructions" "BOTTOMLESS PITS"
contains "$instructions" "SUPER BATS"
contains "$instructions" "THE WUMPUS"
contains "$instructions" "ARROWS: YOU HAVE 5 ARROWS"
contains "$instructions" "WARNINGS:"
contains "$instructions" "YOU ARE IN ROOM 1"

skip="$(printf 'n\n' | bin/htw --player 1 --wumpus 13 --pits 14,15 --bats 16,17)"
contains "$skip" "YOU ARE IN ROOM 1"
contains "$skip" "SHOOT OR MOVE (S-M)?"
if grep -Fq "WELCOME TO 'HUNT THE WUMPUS'" <<<"$skip"; then
  echo "instructions were printed despite skip answer" >&2
  exit 1
fi

turn="$(printf 'n\n' | bin/htw --player 1 --wumpus 2 --pits 5,14 --bats 8,17 --arrows 5)"
contains "$turn" "YOU ARE IN ROOM 1"
contains "$turn" "TUNNELS LEAD TO 2 5 8"
contains "$turn" "I SMELL A WUMPUS"
contains "$turn" "BATS NEARBY"
contains "$turn" "I FEEL A DRAFT"
contains "$turn" "ARROWS LEFT: 5"

move="$(printf 'n\nm 2\n' | bin/htw --player 1 --wumpus 13 --pits 14,15 --bats 16,17)"
contains "$move" "YOU ARE IN ROOM 2"

win="$(printf 'n\ns 2\n' | bin/htw --player 1 --wumpus 2 --pits 14,15 --bats 16,17)"
contains "$win" "AHA! YOU GOT THE WUMPUS!"
contains "$win" "HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!"

invalid="$(printf 'n\nx\nm 3\ns\n' | bin/htw --player 1 --wumpus 13 --pits 14,15 --bats 16,17 --arrows 5)"
contains "$invalid" "X IS NOT A COMMAND"
contains "$invalid" "CAN'T MOVE THERE"
contains "$invalid" "CAN'T SHOOT THERE"
contains "$invalid" "ARROWS LEFT: 5"

pit="$(printf 'n\nm 2\n' | bin/htw --player 1 --wumpus 13 --pits 2,15 --bats 16,17)"
contains "$pit" "YYYIIIIEEEE . . . FELL IN PIT"
contains "$pit" "HA HA HA - YOU LOSE!"
contains "$pit" "SAME SET UP (Y-N)?"

bat="$(printf 'n\nm 2\n' | bin/htw --player 1 --wumpus 13 --pits 10,15 --bats 2,17 --bat-transport 10)"
contains "$bat" "ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!"
contains "$bat" "YYYIIIIEEEE . . . FELL IN PIT"
contains "$bat" "HA HA HA - YOU LOSE!"

self_hit="$(printf 'n\ns 2 1\n' | bin/htw --player 1 --wumpus 13 --pits 14,15 --bats 16,17)"
contains "$self_hit" "OOPS! ARROW GOT YOU!"
contains "$self_hit" "HA HA HA - YOU LOSE!"

wumpus_eats="$(printf 'n\ns 5\n' | bin/htw --player 1 --wumpus 10 --pits 14,15 --bats 16,17 --arrows 5 --wumpus-wake 1)"
contains "$wumpus_eats" "HA HA HA - YOU LOSE!"

out_of_arrows="$(printf 'n\ns 5\n' | bin/htw --player 1 --wumpus 10 --pits 14,15 --bats 16,17 --arrows 1 --wumpus-wake stay)"
contains "$out_of_arrows" "YOU RAN OUT OF ARROWS"
contains "$out_of_arrows" "HA HA HA - YOU LOSE!"

same_setup="$(printf 'n\nm 2\ny\n' | bin/htw --player 1 --wumpus 13 --pits 2,15 --bats 16,17)"
contains "$same_setup" "SAME SET UP (Y-N)?"
contains "$same_setup" "YOU ARE IN ROOM 1"

new_setup="$(printf 'n\nm 2\nn\n' | bin/htw --player 1 --wumpus 13 --pits 2,15 --bats 16,17)"
contains "$new_setup" "SAME SET UP (Y-N)?"
contains "$new_setup" "YOU ARE IN ROOM"
contains "$new_setup" "SHOOT OR MOVE (S-M)?"
