# mutation-stamp: sha256=699cbcceaa15a17ec3abeb92fb0909f1e4049dee0c84d1167d2064f0dfc2a41b
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-05T20:26:37Z","feature_name":"Turn display","feature_path":"features/ui/turn-display.feature","background_hash":"c81f3d21606ea9059fbcd383986985c848786bbd2016bac2375baa8d039552e9","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Turn display 001: each turn shows room tunnels warnings arrows and prompt","scenario_hash":"891bafded2fd5b4faeec8188357a29dbc92fdf32abc4fda489fe4759a9e8569e","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:25:19Z"},{"index":1,"name":"Turn display 002: warning-free turn shows no hazard warnings","scenario_hash":"19505a4b0878d18ef350f0bd231f14d6f3952d7cf478675b47ab117b2753963a","mutation_count":12,"result":{"Total":12,"Killed":12,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:25:19Z"},{"index":2,"name":"Turn display 003: instructions can be shown or skipped","scenario_hash":"1b1372c1b50834eb51633993a2bb4b86984719cdbd1b1831d390efd0d800480c","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:25:19Z"}]}
# acceptance-mutation-manifest-end

Feature: Turn display

  Background:
    Given the terminal game is started with a scripted setup

  # Turn display 001
  Scenario Outline: Turn display 001: each turn shows room tunnels warnings arrows and prompt
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the player has <arrows> arrows
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
        When the next turn is displayed
        Then the output contains line <room_line>
    And the output contains line <tunnel_line>
    And the output contains warnings <warnings>
    And the output contains line <arrows_line>
    And the output contains prompt <prompt>

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | room_line            | tunnel_line              | warnings                                      | arrows_line       | prompt                 |
          | 1           | 2           | 5, 14     | 8, 17     | 5      | 1                    | 2                    | 5, 14              | 8, 17              | 5                        | YOU ARE IN ROOM 1    | TUNNELS LEAD TO 2 5 8    | I SMELL A WUMPUS, BATS NEARBY, I FEEL A DRAFT | ARROWS LEFT: 5    | SHOOT OR MOVE (S-M)?  |

  # Turn display 002
  Scenario Outline: Turn display 002: warning-free turn shows no hazard warnings
    Given the player is in room <player_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the next turn is displayed
        Then the output contains line <room_line>
    And the output contains line <tunnel_line>
    And the output contains warnings <warnings>
    And the output contains prompt <prompt>

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | room_line         | tunnel_line           | warnings | prompt                 |
          | 1           | 6           | 3, 4      | 7, 9      | 1                    | 6                    | 3, 4               | 7, 9               | YOU ARE IN ROOM 1 | TUNNELS LEAD TO 2 5 8 | none     | SHOOT OR MOVE (S-M)?  |

  # Turn display 003
  Scenario Outline: Turn display 003: instructions can be shown or skipped
        Given the terminal game has not started play
        When the player answers instructions prompt with <answer>
        Then the instructions answer is <expected_answer>
        And the output includes instructions text <includes_instructions>
        And the first turn is displayed

        Examples:
          | answer | expected_answer | includes_instructions |
          | y      | y               | true                  |
          | n      | n               | false                 |
