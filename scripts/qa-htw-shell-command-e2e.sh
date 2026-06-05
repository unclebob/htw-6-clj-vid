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

export PATH="$PWD/bin:$PATH"

plain="$(printf 'n\n' | htw)"
contains "$plain" "INSTRUCTIONS (Y-N)?"
contains "$plain" "YOU ARE IN ROOM"
contains "$plain" "SHOOT OR MOVE (S-M)?"

observed_one="$(printf 'n\n' | htw --show-seed true)"
observed_two="$(printf 'n\n' | htw --show-seed true)"
seed_one="$(awk '/^SEED: /{print $2; exit}' <<<"$observed_one")"
seed_two="$(awk '/^SEED: /{print $2; exit}' <<<"$observed_two")"
if [[ -z "$seed_one" || -z "$seed_two" || "$seed_one" == "$seed_two" ]]; then
  echo "plain shell launches did not expose distinct random seeds" >&2
  exit 1
fi
contains "$observed_one" "SHOOT OR MOVE (S-M)?"
contains "$observed_two" "SHOOT OR MOVE (S-M)?"

deterministic_one="$(bb inspect --seed 1973)"
deterministic_two="$(bb inspect --seed 1973)"
if [[ "$deterministic_one" != "$deterministic_two" ]]; then
  echo "deterministic QA seed did not repeat setup" >&2
  exit 1
fi

plain_after_seed="$(printf 'n\n' | htw --show-seed true)"
seed_after="$(awk '/^SEED: /{print $2; exit}' <<<"$plain_after_seed")"
if [[ "$seed_after" == "1973" ]]; then
  echo "plain htw launch reused deterministic QA seed" >&2
  exit 1
fi
contains "$plain_after_seed" "SHOOT OR MOVE (S-M)?"
