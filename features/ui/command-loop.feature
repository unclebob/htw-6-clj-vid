Feature: Command loop

  Background:
    Given the terminal game is started with a scripted setup

  # Command loop 001
  Scenario Outline: Command loop 001: move command is case insensitive
    Given the player is in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player enters command <command>
    Then the next turn shows room <expected_room>
    And the game is still in progress

    Examples:
      | start_room | wumpus_room | pit_rooms | bat_rooms | command | expected_room |
      | 1          | 13          | 14, 15    | 16, 17    | m 2     | 2             |
      | 1          | 13          | 14, 15    | 16, 17    | M 5     | 5             |

  # Command loop 002
  Scenario Outline: Command loop 002: shoot command is case insensitive
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player enters command <command>
    Then the game is won
    And the output contains line <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | command | message                    |
      | 1           | 2           | 14, 15    | 16, 17    | s 2     | AHA! YOU GOT THE WUMPUS!   |
      | 1           | 5           | 14, 15    | 16, 17    | S 5     | AHA! YOU GOT THE WUMPUS!   |

  # Command loop 003
  Scenario Outline: Command loop 003: unknown command is rejected without advancing
    Given the player is in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player enters command <command>
    Then the output contains line <message>
    And the next prompt shows room <start_room>
    And the game is still in progress

    Examples:
      | start_room | wumpus_room | pit_rooms | bat_rooms | command | message            |
      | 1          | 13          | 14, 15    | 16, 17    | x       | X IS NOT A COMMAND |

  # Command loop 004
  Scenario Outline: Command loop 004: impossible commands are rejected without advancing
    Given the player is in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <arrows> arrows
    When the player enters command <command>
    Then the output contains line <message>
    And the next prompt shows room <start_room>
    And the player still has <arrows> arrows
    And the game is still in progress

    Examples:
      | start_room | wumpus_room | pit_rooms | bat_rooms | arrows | command              | message             |
      | 1          | 13          | 14, 15    | 16, 17    | 5      | m 3                  | CAN'T MOVE THERE   |
      | 1          | 13          | 14, 15    | 16, 17    | 5      | s                    | CAN'T SHOOT THERE  |
      | 1          | 13          | 14, 15    | 16, 17    | 5      | s 2 10 11 12 13 14   | CAN'T SHOOT THERE  |
