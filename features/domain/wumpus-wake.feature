# mutation-stamp: sha256=d60772379ab78b58a963d556e3cecc11d7e40ba94527d48b022991b6e0c42d41
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-05T20:26:36Z","feature_name":"Wumpus wake and movement","feature_path":"features/domain/wumpus-wake.feature","background_hash":"13d117037c0f5f98733b41ac1c2391a100cd6a00f807de23835bb9e0ec63dd85","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Wumpus wake and movement 001: entering the Wumpus room wakes it","scenario_hash":"de3eadb80e0a9f18917f944957ae31d5e7682d54f9cde4b989917c642db0fe23","mutation_count":26,"result":{"Total":26,"Killed":26,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:05:06Z"},{"index":1,"name":"Wumpus wake and movement 002: Wumpus wake choices are limited to its room and neighbors","scenario_hash":"e70be945e55b51b3c2b9969da6391b39561fa1aea28a5ed4097493405cc505c8","mutation_count":8,"result":{"Total":8,"Killed":8,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:05:06Z"},{"index":2,"name":"Wumpus wake and movement 003: Wumpus is immune to pits and bats while moving","scenario_hash":"d1b14c9434b9bc3a859a4f74990ae9f08dbd8eb91bcc848b1331f2ff2dfe4698","mutation_count":20,"result":{"Total":20,"Killed":20,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:05:06Z"}]}
# acceptance-mutation-manifest-end

Feature: Wumpus wake and movement

  Background:
    Given a new Hunt the Wumpus game specification

  # Wumpus wake and movement 001
  Scenario Outline: Wumpus wake and movement 001: entering the Wumpus room wakes it
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <setup_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    And the Wumpus wake choice is <wake_choice>
    When the player moves to room <destination_room>
    Then the requested move is <expected_destination_room>
    And the Wumpus is in room <expected_wumpus_room>
    And the game is <outcome>

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_destination_room | setup_wumpus_room | expected_pit_rooms | expected_bat_rooms | wake_choice | expected_wumpus_room | outcome     |
      | 1          | 2                | 2           | 14, 15    | 16, 17    | 1                    | 2                         | 2                 | 14, 15             | 16, 17             | stay        | 2                    | lost        |
      | 1          | 2                | 2           | 14, 15    | 16, 17    | 1                    | 2                         | 2                 | 14, 15             | 16, 17             | move to 3   | 3                    | in progress |

  # Wumpus wake and movement 002
  Scenario Outline: Wumpus wake and movement 002: Wumpus wake choices are limited to its room and neighbors
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    When Wumpus wake options are requested
    Then the player is in room <expected_player_room>
    And the Wumpus wake options are <wake_options>

    Examples:
      | player_room | wumpus_room | expected_player_room | wake_options     |
      | 1           | 10          | 1                    | 10, 2, 9, 11     |
      | 5           | 20          | 5                    | 20, 13, 16, 19   |

  # Wumpus wake and movement 003
  Scenario Outline: Wumpus wake and movement 003: Wumpus is immune to pits and bats while moving
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <setup_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    And the Wumpus wake choice is <wake_choice>
    When the Wumpus wakes
    Then the Wumpus is in room <expected_wumpus_room>
    And the game is still in progress

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | setup_wumpus_room | expected_pit_rooms | expected_bat_rooms | wake_choice | expected_wumpus_room |
      | 1           | 10          | 2, 14     | 9, 17     | 1                    | 10                | 2, 14              | 9, 17              | move to 2   | 2                    |
      | 1           | 10          | 2, 14     | 9, 17     | 1                    | 10                | 2, 14              | 9, 17              | move to 9   | 9                    |
