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

safe="$(bb inspect --player 1 --wumpus 13 --pits 14,15 --bats 16,17 --commands "m 2")"
contains "$safe" "YOU ARE IN ROOM: 1"
contains "$safe" "TUNNELS: 2, 5, 8"
contains "$safe" "PLAYER: 2"
contains "$safe" "STATUS: IN-PROGRESS"

invalid="$(bb inspect --player 1 --wumpus 13 --pits 14,15 --bats 16,17 --commands "m 3")"
contains "$invalid" "CAN'T MOVE THERE"
contains "$invalid" "PLAYER: 1"
contains "$invalid" "STATUS: IN-PROGRESS"

pit="$(bb inspect --player 1 --wumpus 13 --pits 2,15 --bats 16,17 --commands "m 2")"
contains "$pit" "YYYIIIIEEEE . . . FELL IN PIT"
contains "$pit" "STATUS: LOST"

bat_safe="$(bb inspect --player 1 --wumpus 13 --pits 14,15 --bats 2,17 --bat-transport 10 --commands "m 2")"
contains "$bat_safe" "ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!"
contains "$bat_safe" "PLAYER: 10"
contains "$bat_safe" "STATUS: IN-PROGRESS"

bat_pit="$(bb inspect --player 1 --wumpus 13 --pits 10,15 --bats 2,17 --bat-transport 10 --commands "m 2")"
contains "$bat_pit" "ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!"
contains "$bat_pit" "YYYIIIIEEEE . . . FELL IN PIT"
contains "$bat_pit" "STATUS: LOST"

bat_wumpus="$(bb inspect --player 1 --wumpus 10 --pits 14,15 --bats 2,17 --bat-transport 10 --wumpus-wake stay --commands "m 2")"
contains "$bat_wumpus" "ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!"
contains "$bat_wumpus" "STATUS: LOST"

warnings="$(bb inspect --player 1 --wumpus 2 --pits 5,14 --bats 8,17 --commands "m 2")"
contains "$warnings" "I SMELL A WUMPUS"
contains "$warnings" "BATS NEARBY"
contains "$warnings" "I FEEL A DRAFT"

no_warnings="$(bb inspect --player 1 --wumpus 6 --pits 3,4 --bats 7,9 --commands "m 2")"
if grep -Eq "I SMELL A WUMPUS|BATS NEARBY|I FEEL A DRAFT" <<<"$no_warnings"; then
  echo "unexpected warning output" >&2
  echo "$no_warnings" >&2
  exit 1
fi

wumpus_move="$(bb inspect --player 1 --wumpus 2 --pits 14,15 --bats 16,17 --wumpus-wake 3 --commands "m 2")"
contains "$wumpus_move" "WUMPUS: 3"
contains "$wumpus_move" "STATUS: IN-PROGRESS"

wumpus_stay="$(bb inspect --player 1 --wumpus 2 --pits 14,15 --bats 16,17 --wumpus-wake stay --commands "m 2")"
contains "$wumpus_stay" "WUMPUS: 2"
contains "$wumpus_stay" "STATUS: LOST"
