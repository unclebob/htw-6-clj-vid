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
    When the next turn is displayed
    Then the output contains line <room_line>
    And the output contains line <tunnel_line>
    And the output contains warnings <warnings>
    And the output contains line <arrows_line>
    And the output contains prompt <prompt>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | arrows | room_line            | tunnel_line              | warnings                                      | arrows_line       | prompt                 |
      | 1           | 2           | 5, 14     | 8, 17     | 5      | YOU ARE IN ROOM 1    | TUNNELS LEAD TO 2 5 8    | I SMELL A WUMPUS, BATS NEARBY, I FEEL A DRAFT | ARROWS LEFT: 5    | SHOOT OR MOVE (S-M)?  |

  # Turn display 002
  Scenario Outline: Turn display 002: warning-free turn shows no hazard warnings
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the next turn is displayed
    Then the output contains line <room_line>
    And the output contains line <tunnel_line>
    And the output contains warnings <warnings>
    And the output contains prompt <prompt>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | room_line         | tunnel_line           | warnings | prompt                 |
      | 1           | 6           | 3, 4      | 7, 9      | YOU ARE IN ROOM 1 | TUNNELS LEAD TO 2 5 8 | none     | SHOOT OR MOVE (S-M)?  |

  # Turn display 003
  Scenario Outline: Turn display 003: instructions can be shown or skipped
    Given the terminal game has not started play
    When the player answers instructions prompt with <answer>
    Then the output includes instructions text <includes_instructions>
    And the first turn is displayed

    Examples:
      | answer | includes_instructions |
      | y      | true                  |
      | n      | false                 |
