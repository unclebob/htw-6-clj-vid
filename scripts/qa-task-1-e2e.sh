#!/usr/bin/env bash
set -euo pipefail

out_one="$(bb inspect --seed 1975)"
out_two="$(bb inspect --seed 1975)"
if [[ "$out_one" != "$out_two" ]]; then
  echo "seeded setup is not reproducible" >&2
  exit 1
fi

for expected in \
  "1: 2, 5, 8" \
  "2: 1, 3, 10" \
  "20: 13, 16, 19" \
  "PLAYER:" \
  "WUMPUS:" \
  "PITS:" \
  "BATS:"; do
  if ! grep -Fq "$expected" <<<"$out_one"; then
    echo "missing expected output: $expected" >&2
    exit 1
  fi
done

same_setup="$(bb inspect --seed 1973 --same-setup true)"
setup_block="$(awk '/^SETUP$/{capture=1; next} /^REUSED SETUP$/{capture=0} capture {print}' <<<"$same_setup")"
reused_block="$(awk '/^REUSED SETUP$/{capture=1; next} capture {print}' <<<"$same_setup")"
if [[ "$setup_block" != "$reused_block" ]]; then
  echo "same setup reuse changed placement" >&2
  exit 1
fi

adjacent="$(bb inspect --player 1 --wumpus 2 --pits 3,4 --bats 5,6 --adjacent 1)"
for expected in \
  "ADJACENT HAZARDS FOR ROOM: 1" \
  "WUMPUS: 1" \
  "PITS: 0" \
  "BATS: 1"; do
  if ! grep -Fq "$expected" <<<"$adjacent"; then
    echo "missing adjacent hazard output: $expected" >&2
    exit 1
  fi
done
