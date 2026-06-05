Feature: Wumpus wake and movement

  Background:
    Given a new Hunt the Wumpus game specification

  # Wumpus wake and movement 001
  Scenario Outline: Wumpus wake and movement 001: entering the Wumpus room wakes it
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the Wumpus wake choice is <wake_choice>
    When the player moves to room <destination_room>
    Then the Wumpus is in room <expected_wumpus_room>
    And the game is <outcome>

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | wake_choice | expected_wumpus_room | outcome      |
      | 1          | 2                | 2           | 14, 15    | 16, 17    | stay        | 2                    | lost         |
      | 1          | 2                | 2           | 14, 15    | 16, 17    | move to 3   | 3                    | in progress  |

  # Wumpus wake and movement 002
  Scenario Outline: Wumpus wake and movement 002: Wumpus wake choices are limited to its room and neighbors
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    When Wumpus wake options are requested
    Then the Wumpus wake options are <wake_options>

    Examples:
      | player_room | wumpus_room | wake_options     |
      | 1           | 10          | 10, 2, 9, 11     |
      | 5           | 20          | 20, 13, 16, 19   |

  # Wumpus wake and movement 003
  Scenario Outline: Wumpus wake and movement 003: Wumpus is immune to pits and bats while moving
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the Wumpus wake choice is <wake_choice>
    When the Wumpus wakes
    Then the Wumpus is in room <expected_wumpus_room>
    And the game is still in progress

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | wake_choice | expected_wumpus_room |
      | 1           | 10          | 2, 14     | 9, 17     | move to 2   | 2                    |
      | 1           | 10          | 2, 14     | 9, 17     | move to 9   | 9                    |
