Feature: Full game outcomes

  Background:
    Given the terminal game is started with a scripted setup

  # Full game outcomes 001
  Scenario Outline: Full game outcomes 001: player wins by shooting the Wumpus
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player enters command <command>
    Then the output contains line <win_message>
    And the output contains line <taunt_message>
    And the game is won

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | command | win_message                | taunt_message                                      |
      | 1           | 2           | 14, 15    | 16, 17    | s 2     | AHA! YOU GOT THE WUMPUS!   | HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!   |

  # Full game outcomes 002
  Scenario Outline: Full game outcomes 002: pit loss shows loss taunt and same setup prompt
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player enters command <command>
    Then the output contains line <loss_message>
    And the output contains line <taunt_message>
    And the output contains prompt <replay_prompt>
    And the game is lost

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | command | loss_message                    | taunt_message          | replay_prompt          |
      | 1           | 13          | 2, 15     | 16, 17    | m 2     | YYYIIIIEEEE . . . FELL IN PIT   | HA HA HA - YOU LOSE!   | SAME SET UP (Y-N)?    |

  # Full game outcomes 003
  Scenario Outline: Full game outcomes 003: bat transport can produce visible loss
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And bat transport will choose room <transport_room>
    When the player enters command <command>
    Then the output contains line <bat_message>
    And the output contains line <loss_message>
    And the output contains line <taunt_message>
    And the game is lost

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | transport_room | command | bat_message                                       | loss_message                    | taunt_message        |
      | 1           | 13          | 10, 15    | 2, 17     | 10             | m 2     | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! | YYYIIIIEEEE . . . FELL IN PIT   | HA HA HA - YOU LOSE! |

  # Full game outcomes 004
  Scenario Outline: Full game outcomes 004: arrow self-hit produces visible loss
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player enters command <command>
    Then the output contains line <loss_message>
    And the output contains line <taunt_message>
    And the game is lost

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | command | loss_message           | taunt_message        |
      | 1           | 13          | 14, 15    | 16, 17    | s 2 1   | OOPS! ARROW GOT YOU!  | HA HA HA - YOU LOSE! |

  # Full game outcomes 005
  Scenario Outline: Full game outcomes 005: missed last arrow produces visible loss
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <arrows> arrows
    And the Wumpus wake choice is <wake_choice>
    When the player enters command <command>
    Then the output contains line <loss_message>
    And the output contains line <taunt_message>
    And the game is lost

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | arrows | wake_choice | command | loss_message          | taunt_message        |
      | 1           | 10          | 14, 15    | 16, 17    | 1      | stay        | s 5     | YOU RAN OUT OF ARROWS | HA HA HA - YOU LOSE! |
