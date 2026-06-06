# mutation-stamp: sha256=d09c78c38d2d68d6b6533fb84f60fa1b28bf6d0d8c2ed0d84b59a6b1f060abba
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T17:22:37Z","feature_name":"Same setup replay","feature_path":"features/ui/replay.feature","background_hash":"c81f3d21606ea9059fbcd383986985c848786bbd2016bac2375baa8d039552e9","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Same setup replay 001: replay with same setup preserves rooms","scenario_hash":"2ba20ed5151f75bcedf7944116d05d6d9894427faf40034f54e73be329a6343c","mutation_count":12,"result":{"Total":12,"Killed":12,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:23:26Z"},{"index":1,"name":"Same setup replay 002: new setup does not require preserved rooms","scenario_hash":"adeb93fefdae62ab22960c0a44e19bd13a927920c7e992a21a86fd674a691a20","mutation_count":12,"result":{"Total":12,"Killed":12,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:23:26Z"}]}
# acceptance-mutation-manifest-end

Feature: Same setup replay

  Background:
    Given the terminal game is started with a scripted setup

  # Same setup replay 001
  Scenario Outline: Same setup replay 001: replay with same setup preserves rooms
    Given the player is in room <player_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the player loses with command <loss_command>
        Then the loss command is <expected_loss_command>
        And the player answers same setup prompt with <answer>
        And the replay answer is <expected_answer>
        And the next game has player room <player_room>
        And the next game has Wumpus room <wumpus_room>
        And the next game has pit rooms <pit_rooms>
        And the next game has bat rooms <bat_rooms>

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | loss_command | expected_loss_command | answer | expected_answer |
          | 1           | 13          | 2, 15     | 16, 17    | 1                    | 13                   | 2, 15              | 16, 17             | m 2          | m 2                   | y      | y               |

  # Same setup replay 002
  Scenario Outline: Same setup replay 002: new setup does not require preserved rooms
    Given the player is in room <player_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the player loses with command <loss_command>
        Then the loss command is <expected_loss_command>
        And the player answers same setup prompt with <answer>
        And the replay answer is <expected_answer>
        And the next game has a valid placement
        And the next game is not required to preserve the previous placement

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | loss_command | expected_loss_command | answer | expected_answer |
          | 1           | 13          | 2, 15     | 16, 17    | 1                    | 13                   | 2, 15              | 16, 17             | m 2          | m 2                   | n      | n               |
