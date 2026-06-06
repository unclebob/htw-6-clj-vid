# mutation-stamp: sha256=718bd89f5fd79c0edc15eecb7a82eb4f2484aa155cb5692c2b6c379f697de0a6
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T16:56:44Z","feature_name":"Reproducible setup","feature_path":"features/domain/reproducible-setup.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Reproducible setup 001: same seed produces identical placement","scenario_hash":"8befe71acce5de6c10151dd0502a59db2cf8b764295afe34cb6711e6cfb55eb7","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:45:01Z"}]}
# acceptance-mutation-manifest-end

Feature: Reproducible setup

  # Reproducible setup 001
  Scenario Outline: Reproducible setup 001: same seed produces identical placement
    When a game is started with seed <seed>
    And a second game is started with seed <seed>
    Then the player room is <player_room>
    And the Wumpus room is <wumpus_room>
    And the pit rooms are <pit_rooms>
    And the bat rooms are <bat_rooms>
    And both games have the same player room
    And both games have the same Wumpus room
    And both games have the same pit rooms
    And both games have the same bat rooms

    Examples:
      | seed | player_room | wumpus_room | pit_rooms | bat_rooms |
      | 1973 | 16          | 17          | 3, 5      | 1, 7      |
      | 1975 | 14          | 13          | 4, 18     | 16, 17    |
      | 1976 | 13          | 17          | 14, 15    | 2, 3      |

  # Reproducible setup 002
  Scenario: Reproducible setup 002: same setup reuse preserves placement
    Given a game was started with seed 1973
    When the same setup is reused
    Then the reused setup has the same player room
    And the reused setup has the same Wumpus room
    And the reused setup has the same pit rooms
    And the reused setup has the same bat rooms
