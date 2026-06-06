# mutation-stamp: sha256=9d8cc4e8201283a6100085db0ee8d0f1f04656f032ca3617e9e345e50c510a23
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T17:22:35Z","feature_name":"Arrow hit order","feature_path":"features/domain/arrow-hit-order.feature","background_hash":"13d117037c0f5f98733b41ac1c2391a100cd6a00f807de23835bb9e0ec63dd85","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Arrow hit order 001: Wumpus hit stops arrow before later player hit","scenario_hash":"8443149b3a17371ddfa0491e2f881d0d4040c8d4335f8049b80288823341524b","mutation_count":28,"result":{"Total":28,"Killed":28,"Survived":0,"Errors":0},"tested_at":"2026-06-06T17:16:00Z"}]}
# acceptance-mutation-manifest-end

Feature: Arrow hit order

  Background:
    Given a new Hunt the Wumpus game specification

  # Arrow hit order 001
  Scenario Outline: Arrow hit order 001: Wumpus hit stops arrow before later player hit
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
    When the player shoots path <path>
    Then the requested path is <expected_path>
    And the arrow visits rooms <visited_rooms>
    And the game is won
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | path        | expected_path | visited_rooms | message                  |
      | 1           | 2           | 14, 15    | 16, 17    | 5               | 1                    | 2                    | 14, 15             | 16, 17             | 5                        | 2, 1        | 2, 1          | 2             | AHA! YOU GOT THE WUMPUS! |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | 1                    | 10                   | 14, 15             | 16, 17             | 5                        | 2, 10, 9, 8, 1 | 2, 10, 9, 8, 1 | 2, 10        | AHA! YOU GOT THE WUMPUS! |
