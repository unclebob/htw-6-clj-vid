Feature: Entity placement

  Background:
    Given a new Hunt the Wumpus game specification

  # Entity placement 001
  Scenario Outline: Entity placement 001: seeded setup occupies distinct valid rooms
    When a game is started with seed <seed>
    Then the player occupies one room
    And the Wumpus occupies one room
    And there are 2 pit rooms
    And there are 2 bat rooms
    And all occupied rooms are distinct
    And every occupied room is numbered from 1 through 20

    Examples:
      | seed |
      | 1001 |
      | 2002 |
      | 3003 |

  # Entity placement 002
  Scenario: Entity placement 002: setup has exactly five hazard rooms
    When a game is started with seed 1973
    Then the Wumpus occupies one room
    And there are 2 pit rooms
    And there are 2 bat rooms
    And there are 5 hazard rooms
    And all hazard rooms are distinct

  # Entity placement 003
  Scenario Outline: Entity placement 003: player never starts on a hazard
    When a game is started with seed <seed>
    Then the player room is not a Wumpus room
    And the player room is not a pit room
    And the player room is not a bat room

    Examples:
      | seed |
      | 404  |
      | 505  |
      | 606  |

  # Entity placement 004
  Scenario Outline: Entity placement 004: adjacent hazard query reports only neighboring hazards
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When adjacent hazards are requested for room <player_room>
    Then the adjacent Wumpus count is <wumpus_count>
    And the adjacent pit count is <pit_count>
    And the adjacent bat count is <bat_count>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | wumpus_count | pit_count | bat_count |
      | 1           | 2           | 3, 4      | 5, 6      | 1            | 0         | 1         |
      | 10          | 11          | 2, 18     | 9, 13     | 1            | 0         | 1         |
      | 20          | 1           | 13, 16    | 18, 19    | 0            | 2         | 1         |
