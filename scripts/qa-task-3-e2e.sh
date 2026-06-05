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

one_room_hit="$(bb inspect --player 1 --wumpus 2 --pits 14,15 --bats 16,17 --arrows 5 --commands "s 2")"
contains "$one_room_hit" "AHA! YOU GOT THE WUMPUS!"
contains "$one_room_hit" "STATUS: WON"

multi_hit="$(bb inspect --player 1 --wumpus 11 --pits 14,15 --bats 16,17 --arrows 5 --commands "s 2 10 11")"
contains "$multi_hit" "AHA! YOU GOT THE WUMPUS!"
contains "$multi_hit" "STATUS: WON"

deviation="$(bb inspect --player 1 --wumpus 13 --pits 14,15 --bats 16,17 --arrows 5 --arrow-deviation 5 --wumpus-wake stay --commands "s 3 4")"
contains "$deviation" "ARROW PATH: 5, 4"
contains "$deviation" "ARROWS: 4"
contains "$deviation" "STATUS: IN-PROGRESS"

deviation_hit="$(bb inspect --player 1 --wumpus 5 --pits 14,15 --bats 16,17 --arrows 5 --arrow-deviation 5 --commands "s 3")"
contains "$deviation_hit" "AHA! YOU GOT THE WUMPUS!"
contains "$deviation_hit" "STATUS: WON"

self_hit="$(bb inspect --player 1 --wumpus 13 --pits 14,15 --bats 16,17 --arrows 5 --commands "s 2 1")"
contains "$self_hit" "OOPS! ARROW GOT YOU!"
contains "$self_hit" "STATUS: LOST"

miss_survives="$(bb inspect --player 1 --wumpus 10 --pits 14,15 --bats 16,17 --arrows 5 --wumpus-wake stay --commands "s 5")"
contains "$miss_survives" "ARROWS: 4"
contains "$miss_survives" "STATUS: IN-PROGRESS"

miss_loses="$(bb inspect --player 1 --wumpus 10 --pits 14,15 --bats 16,17 --arrows 5 --wumpus-wake 1 --commands "s 5")"
contains "$miss_loses" "ARROWS: 4"
contains "$miss_loses" "STATUS: LOST"

last_arrow="$(bb inspect --player 1 --wumpus 10 --pits 14,15 --bats 16,17 --arrows 1 --wumpus-wake stay --commands "s 5")"
contains "$last_arrow" "ARROWS: 0"
contains "$last_arrow" "YOU RAN OUT OF ARROWS"
contains "$last_arrow" "STATUS: LOST"

invalid="$(bb inspect --player 1 --wumpus 10 --pits 14,15 --bats 16,17 --arrows 5 --commands "s;s 2 10 11 12 13 14")"
contains "$invalid" "CAN'T SHOOT THERE"
contains "$invalid" "ARROWS: 5"
contains "$invalid" "STATUS: IN-PROGRESS"
