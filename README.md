# Hunt the Wumpus

Start a playable terminal game from a clean checkout with:

```sh
bin/htw
```

Task 1 exposes a setup-inspection command for QA and early verification:

```sh
bb inspect --seed 1973
```

The command prints the canonical cave exits plus the seeded player, Wumpus, pit,
and bat rooms. Use `--same-setup true` to print the reused setup, and use an
explicit setup with `--adjacent` to inspect neighboring hazards:

```sh
bb inspect --player 1 --wumpus 2 --pits 3,4 --bats 5,6 --adjacent 1
```

The same command supports scripted movement and shooting checks through the
visible command UI. Separate commands with semicolons:

```sh
bb inspect --player 1 --wumpus 13 --pits 14,15 --bats 16,17 --commands "m 2"
bb inspect --player 1 --wumpus 11 --pits 14,15 --bats 16,17 --commands "s 2 10 11"
```

Deterministic QA options include `--bat-transport`, `--wumpus-wake`,
`--arrow-deviation`, and `--arrows`.

The shell command also accepts the same deterministic setup options for
end-to-end QA runs. Use `--show-seed true` to print the random startup seed
without making plain `bin/htw` deterministic.
