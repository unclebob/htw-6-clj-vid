# mutation-stamp: sha256=00e923f1fcd088ec2a8905c073397ad527bbc92f445868cfb9a8a3332863cf9f
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T16:42:43Z","feature_name":"Full game outcomes","feature_path":"features/ui/full-game-outcomes.feature","background_hash":"c81f3d21606ea9059fbcd383986985c848786bbd2016bac2375baa8d039552e9","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Full game outcomes 001: player wins by shooting the Wumpus","scenario_hash":"0438716af1eac8f2d884aacfc5429456ee057f81de1788b863bf09e3bfa8c351","mutation_count":12,"result":{"Total":12,"Killed":12,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:22:16Z"},{"index":1,"name":"Full game outcomes 002: pit loss shows loss taunt and same setup prompt","scenario_hash":"e39527236e1cc24670b5c34450d85507dae55f6c177ab753afd2df7866f1c1b4","mutation_count":13,"result":{"Total":13,"Killed":13,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:22:16Z"},{"index":2,"name":"Full game outcomes 003: bat transport can produce visible loss","scenario_hash":"eecbe2278d46e1d7cc9d777540278bf163e1e62d53fd0815d46328a7bc956b0f","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:22:16Z"},{"index":3,"name":"Full game outcomes 004: arrow self-hit produces visible loss","scenario_hash":"4bdf82b570f0eb0f3f791f54941935040f870af3dfdb13df4eebce8916cd8004","mutation_count":12,"result":{"Total":12,"Killed":12,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:22:16Z"},{"index":4,"name":"Full game outcomes 005: missed last arrow produces visible loss","scenario_hash":"c06c28ac9e30f83a959b700af2b2e0e05f7cade5fe35f89abc3b2073857b82d6","mutation_count":16,"result":{"Total":16,"Killed":16,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:22:16Z"}]}
# acceptance-mutation-manifest-end

Feature: Full game outcomes

  Background:
    Given the terminal game is started with a scripted setup

  # Full game outcomes 001
  Scenario Outline: Full game outcomes 001: player wins by shooting the Wumpus
    Given the player is in room <player_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the output contains line <win_message>
        And the output contains line <taunt_message>
        And the game is won

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | command | expected_command | win_message                | taunt_message                                      |
          | 1           | 2           | 14, 15    | 16, 17    | 1                    | 2                    | 14, 15             | 16, 17             | s 2     | s 2              | AHA! YOU GOT THE WUMPUS!   | HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!   |

  # Full game outcomes 002
  Scenario Outline: Full game outcomes 002: pit loss shows loss taunt and same setup prompt
    Given the player is in room <player_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the output contains line <loss_message>
        And the output contains line <taunt_message>
        And the output contains prompt <replay_prompt>
        And the game is lost

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | command | expected_command | loss_message                    | taunt_message          | replay_prompt          |
          | 1           | 13          | 2, 15     | 16, 17    | 1                    | 13                   | 2, 15              | 16, 17             | m 2     | m 2              | YYYIIIIEEEE . . . FELL IN PIT   | HA HA HA - YOU LOSE!   | SAME SET UP (Y-N)?    |

  # Full game outcomes 003
  Scenario Outline: Full game outcomes 003: bat transport can produce visible loss
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And bat transport will choose room <transport_room>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        And the configured bat transport room is <expected_transport_room>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the output contains line <bat_message>
        And the output contains line <loss_message>
        And the output contains line <taunt_message>
        And the game is lost

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | transport_room | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_transport_room | command | expected_command | bat_message                                       | loss_message                    | taunt_message        |
          | 1           | 13          | 10, 15    | 2, 17     | 10             | 1                    | 13                   | 10, 15             | 2, 17              | 10                      | m 2     | m 2              | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! | YYYIIIIEEEE . . . FELL IN PIT   | HA HA HA - YOU LOSE! |

  # Full game outcomes 004
  Scenario Outline: Full game outcomes 004: arrow self-hit produces visible loss
    Given the player is in room <player_room>
        And the Wumpus is in room <wumpus_room>
        And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the output contains line <loss_message>
        And the output contains line <taunt_message>
        And the game is lost

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | command | expected_command | loss_message           | taunt_message        |
          | 1           | 13          | 14, 15    | 16, 17    | 1                    | 13                   | 14, 15             | 16, 17             | s 2 1   | s 2 1            | OOPS! ARROW GOT YOU!  | HA HA HA - YOU LOSE! |

  # Full game outcomes 005
  Scenario Outline: Full game outcomes 005: missed last arrow produces visible loss
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
        And bats are in rooms <bat_rooms>
        And the player has <arrows> arrows
        And the Wumpus wake choice is <wake_choice>
        And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
        And the configured Wumpus wake choice is <expected_wake_choice>
        When the player enters command <command>
        Then the entered command is <expected_command>
        And the output contains line <loss_message>
        And the output contains line <taunt_message>
        And the game is lost

        Examples:
          | player_room | wumpus_room | pit_rooms | bat_rooms | arrows | wake_choice | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | expected_wake_choice | command | expected_command | loss_message          | taunt_message        |
          | 1           | 10          | 14, 15    | 16, 17    | 1      | stay        | 1                    | 10                   | 14, 15             | 16, 17             | 1                        | stay                 | s 5     | s 5              | YOU RAN OUT OF ARROWS | HA HA HA - YOU LOSE! |
