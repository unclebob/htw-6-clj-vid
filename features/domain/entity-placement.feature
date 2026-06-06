# mutation-stamp: sha256=c94fd429cd3fb695a9b45a24946302c8bad85e04cf4a0ceb2e1f58d673441eec
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T16:56:43Z","feature_name":"Entity placement","feature_path":"features/domain/entity-placement.feature","background_hash":"13d117037c0f5f98733b41ac1c2391a100cd6a00f807de23835bb9e0ec63dd85","implementation_hash":"unknown","scenarios":[{"index":3,"name":"Entity placement 004: adjacent hazard query reports only neighboring hazards","scenario_hash":"1e5f973e99bfc1a605813b968184f73d8073b91a82757a2d0118140dd986c0d8","mutation_count":30,"result":{"Total":30,"Killed":30,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:44:13Z"},{"index":0,"name":"Entity placement 001: seeded setup occupies distinct valid rooms","scenario_hash":"c7cc10657cd899e4897431ae0629e3394b21120e301a2ee46c4e38b685ca8c20","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:42:21Z"},{"index":2,"name":"Entity placement 003: player never starts on a hazard","scenario_hash":"0537f4b10ca2b15642fd01995064e2bbcc67cc2effd217714021151aeb8dbe9d","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:42:21Z"}]}
# acceptance-mutation-manifest-end

Feature: Entity placement

  Background:
    Given a new Hunt the Wumpus game specification

  # Entity placement 001
  Scenario Outline: Entity placement 001: seeded setup occupies distinct valid rooms
    When a game is started with seed <seed>
    Then the player occupies one room
    And the player room is <player_room>
    And the Wumpus occupies one room
    And the Wumpus room is <wumpus_room>
    And there are 2 pit rooms
    And the pit rooms are <pit_rooms>
    And there are 2 bat rooms
    And the bat rooms are <bat_rooms>
    And all occupied rooms are distinct
    And every occupied room is numbered from 1 through 20

    Examples:
      | seed | player_room | wumpus_room | pit_rooms | bat_rooms |
      | 1001 | 17          | 7           | 11, 12    | 16, 18    |
      | 2002 | 18          | 7           | 2, 12     | 1, 13     |
      | 3003 | 10          | 2           | 14, 19    | 17, 18    |

  # Entity placement 002
  Scenario: Entity placement 002: setup has exactly five hazard rooms
    When a game is started with seed 1973
    Then the Wumpus occupies one room
    And there are 2 pit rooms
    And there are 2 bat rooms
    And there are 5 hazard rooms
    And all hazard rooms are distinct

  # Entity placement 003
  Scenario Outline: Entity placement 003: player never starts on a hazard
    When a game is started with seed <seed>
    Then the player room is <player_room>
    And the Wumpus room is <wumpus_room>
    And the pit rooms are <pit_rooms>
    And the bat rooms are <bat_rooms>
    And the player room is not a Wumpus room
    And the player room is not a pit room
    And the player room is not a bat room

    Examples:
      | seed | player_room | wumpus_room | pit_rooms | bat_rooms |
      | 404  | 18          | 6           | 5, 14     | 8, 10     |
      | 505  | 18          | 1           | 8, 12     | 5, 11     |
      | 606  | 16          | 19          | 6, 9      | 10, 13    |

  # Entity placement 004
  Scenario Outline: Entity placement 004: adjacent hazard query reports only neighboring hazards
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When adjacent hazards are requested for room <player_room>
    Then the player room is <player_room>
    And the Wumpus room is <expected_wumpus_room>
    And the pit rooms are <expected_pit_rooms>
    And the bat rooms are <expected_bat_rooms>
    And the adjacent Wumpus count is <wumpus_count>
    And the adjacent pit count is <pit_count>
    And the adjacent bat count is <bat_count>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | wumpus_count | pit_count | bat_count |
      | 1           | 2           | 3, 4      | 5, 6      | 2                    | 3, 4               | 5, 6               | 1            | 0         | 1         |
      | 10          | 11          | 2, 18     | 9, 13     | 11                   | 2, 18              | 9, 13              | 1            | 1         | 1         |
      | 20          | 1           | 13, 16    | 18, 19    | 1                    | 13, 16             | 18, 19             | 0            | 2         | 1         |
