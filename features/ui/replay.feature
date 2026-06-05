Feature: Same setup replay

  Background:
    Given the terminal game is started with a scripted setup

  # Same setup replay 001
  Scenario Outline: Same setup replay 001: replay with same setup preserves rooms
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player loses with command <loss_command>
    And the player answers same setup prompt with <answer>
    Then the next game has player room <player_room>
    And the next game has Wumpus room <wumpus_room>
    And the next game has pit rooms <pit_rooms>
    And the next game has bat rooms <bat_rooms>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | loss_command | answer |
      | 1           | 13          | 2, 15     | 16, 17    | m 2          | y      |

  # Same setup replay 002
  Scenario Outline: Same setup replay 002: new setup does not require preserved rooms
    Given the player is in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player loses with command <loss_command>
    And the player answers same setup prompt with <answer>
    Then the next game has a valid placement
    And the next game is not required to preserve the previous placement

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | loss_command | answer |
      | 1           | 13          | 2, 15     | 16, 17    | m 2          | n      |
