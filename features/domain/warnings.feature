# mutation-stamp: sha256=01ab24b30218003c97d7e7dae4f417aa753bc92b9aae350892994d50acda0745
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T17:22:36Z","feature_name":"Adjacent hazard warnings","feature_path":"features/domain/warnings.feature","background_hash":"13d117037c0f5f98733b41ac1c2391a100cd6a00f807de23835bb9e0ec63dd85","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Adjacent hazard warnings 001: warnings report only adjacent hazards","scenario_hash":"6db7174b331f6a57b6f5d0cef1060e7c742c5d8faf3c71f30c5eecc87703fb3b","mutation_count":45,"result":{"Total":45,"Killed":45,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:03:44Z"},{"index":1,"name":"Adjacent hazard warnings 002: duplicate hazard types produce one warning line","scenario_hash":"629c96b38fa6aa011c966c69b60f0d23dea585a9d392ce1abd91de58302825bf","mutation_count":18,"result":{"Total":18,"Killed":18,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:03:44Z"},{"index":2,"name":"Adjacent hazard warnings 003: warning order is stable","scenario_hash":"b4c5e5b6590b34c32f4855915ff1301168f819389b1d14695759ab35ebd8332f","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:03:44Z"}]}
# acceptance-mutation-manifest-end

Feature: Adjacent hazard warnings

  Background:
    Given a new Hunt the Wumpus game specification

  # Adjacent hazard warnings 001
  Scenario Outline: Adjacent hazard warnings 001: warnings report only adjacent hazards
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    When turn warnings are requested
    Then the visible warnings are <warnings>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | warnings                                       |
      | 1           | 2           | 3, 4      | 6, 7      | 1                    | 2                    | 3, 4               | 6, 7               | I SMELL A WUMPUS                              |
      | 1           | 6           | 2, 4      | 7, 9      | 1                    | 6                    | 2, 4               | 7, 9               | I FEEL A DRAFT                                |
      | 1           | 6           | 3, 4      | 5, 7      | 1                    | 6                    | 3, 4               | 5, 7               | BATS NEARBY                                   |
      | 1           | 2           | 5, 14     | 8, 17     | 1                    | 2                    | 5, 14              | 8, 17              | I SMELL A WUMPUS, BATS NEARBY, I FEEL A DRAFT |
      | 1           | 6           | 3, 4      | 7, 9      | 1                    | 6                    | 3, 4               | 7, 9               | none                                          |

  # Adjacent hazard warnings 002
  Scenario Outline: Adjacent hazard warnings 002: duplicate hazard types produce one warning line
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    When turn warnings are requested
    Then the visible warnings are <warnings>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | warnings       |
      | 1           | 6           | 2, 5      | 7, 9      | 1                    | 6                    | 2, 5               | 7, 9               | I FEEL A DRAFT |
      | 1           | 6           | 3, 4      | 2, 5      | 1                    | 6                    | 3, 4               | 2, 5               | BATS NEARBY    |

  # Adjacent hazard warnings 003
  Scenario Outline: Adjacent hazard warnings 003: warning order is stable
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    When turn warnings are requested
    Then warning number 1 is <first_warning>
    And warning number 2 is <second_warning>
    And warning number 3 is <third_warning>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | first_warning    | second_warning | third_warning  |
      | 1           | 2           | 5, 14     | 8, 17     | 1                    | 2                    | 5, 14              | 8, 17              | I SMELL A WUMPUS | BATS NEARBY    | I FEEL A DRAFT |
