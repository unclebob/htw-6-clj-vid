# mutation-stamp: sha256=f3699defcbeae021fa2e3110ca4045f17ce840644a38271bc49ae2a068adc8a6
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T16:42:43Z","feature_name":"Command loop","feature_path":"features/ui/command-loop.feature","background_hash":"c81f3d21606ea9059fbcd383986985c848786bbd2016bac2375baa8d039552e9","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Command loop 001: move command is case insensitive","scenario_hash":"90caabf40081b93fd8b9942216539e83def8f27e9ff806e6d7b4e8d96385049f","mutation_count":22,"result":{"Total":22,"Killed":22,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:19:48Z"},{"index":1,"name":"Command loop 002: shoot command is case insensitive","scenario_hash":"82bba40657e31d27012d5877d4160b84be70bb3c2537c09f1c44750467097a39","mutation_count":22,"result":{"Total":22,"Killed":22,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:19:48Z"},{"index":2,"name":"Command loop 003: unknown command is rejected without advancing","scenario_hash":"6bc3625e6492e3262980db0ff176641820a367c132fd58bb659bb03092dab9b2","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:19:48Z"},{"index":3,"name":"Command loop 004: impossible commands are rejected without advancing","scenario_hash":"5b72e188eafe817ff77f25d508c44ee76e70353c3f02e006e608ecc2c6a8c030","mutation_count":39,"result":{"Total":39,"Killed":39,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:19:48Z"}]}
# acceptance-mutation-manifest-end

Feature: Command loop

  Background:
    Given the terminal game is started with a scripted setup

  # Command loop 001
  Scenario Outline: Command loop 001: move command is case insensitive
    Given the player is in room <start_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the next turn shows room <expected_room>
        And the game is still in progress

        Examples:
          | start_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | command | expected_command | expected_room |
          | 1          | 13          | 14, 15    | 16, 17    | 1                    | 13                   | 14, 15             | 16, 17             | m 2     | m 2              | 2             |
          | 1          | 13          | 14, 15    | 16, 17    | 1                    | 13                   | 14, 15             | 16, 17             | M 5     | M 5              | 5             |

  # Command loop 002
  Scenario Outline: Command loop 002: shoot command is case insensitive
    Given the player is in room <player_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the game is won
        And the output contains line <message>

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | command | expected_command | message                    |
          | 1           | 2           | 14, 15    | 16, 17    | 1                    | 2                    | 14, 15             | 16, 17             | s 2     | s 2              | AHA! YOU GOT THE WUMPUS!   |
          | 1           | 5           | 14, 15    | 16, 17    | 1                    | 5                    | 14, 15             | 16, 17             | S 5     | S 5              | AHA! YOU GOT THE WUMPUS!   |

  # Command loop 003
  Scenario Outline: Command loop 003: unknown command is rejected without advancing
    Given the player is in room <start_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the output contains line <message>
        And the next prompt shows room <start_room>
        And the game is still in progress

        Examples:
          | start_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | command | expected_command | message            |
          | 1          | 13          | 14, 15    | 16, 17    | 1                    | 13                   | 14, 15             | 16, 17             | x       | x                | X IS NOT A COMMAND |

  # Command loop 004
  Scenario Outline: Command loop 004: impossible commands are rejected without advancing
    Given the player is in room <start_room>
    And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the player has <arrows> arrows
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the output contains line <message>
        And the next prompt shows room <start_room>
        And the player still has <arrows> arrows
        And the game is still in progress

        Examples:
          | start_room | wumpus_room | pit_rooms | bat_rooms | arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | command              | expected_command     | message             |
          | 1          | 13          | 14, 15    | 16, 17    | 5      | 1                    | 13                   | 14, 15             | 16, 17             | 5                        | m 3                  | m 3                  | CAN'T MOVE THERE   |
          | 1          | 13          | 14, 15    | 16, 17    | 5      | 1                    | 13                   | 14, 15             | 16, 17             | 5                        | s                    | s                    | CAN'T SHOOT THERE  |
          | 1          | 13          | 14, 15    | 16, 17    | 5      | 1                    | 13                   | 14, 15             | 16, 17             | 5                        | s 2 10 11 12 13 14   | s 2 10 11 12 13 14   | CAN'T SHOOT THERE  |
