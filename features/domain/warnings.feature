Feature: Adjacent hazard warnings

  Background:
    Given a new Hunt the Wumpus game specification

  # Adjacent hazard warnings 001
  Scenario Outline: Adjacent hazard warnings 001: warnings report only adjacent hazards
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When turn warnings are requested
    Then the visible warnings are <warnings>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | warnings                                      |
      | 1           | 2           | 3, 4      | 6, 7      | I SMELL A WUMPUS                             |
      | 1           | 6           | 2, 4      | 7, 9      | I FEEL A DRAFT                               |
      | 1           | 6           | 3, 4      | 5, 7      | BATS NEARBY                                  |
      | 1           | 2           | 5, 14     | 8, 17     | I SMELL A WUMPUS, BATS NEARBY, I FEEL A DRAFT |
      | 1           | 6           | 3, 4      | 7, 9      | none                                         |

  # Adjacent hazard warnings 002
  Scenario Outline: Adjacent hazard warnings 002: duplicate hazard types produce one warning line
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When turn warnings are requested
    Then the visible warnings are <warnings>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | warnings                       |
      | 1           | 6           | 2, 5      | 7, 9      | I FEEL A DRAFT                 |
      | 1           | 6           | 3, 4      | 2, 5      | BATS NEARBY                    |

  # Adjacent hazard warnings 003
  Scenario Outline: Adjacent hazard warnings 003: warning order is stable
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When turn warnings are requested
    Then warning number 1 is <first_warning>
    And warning number 2 is <second_warning>
    And warning number 3 is <third_warning>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | first_warning     | second_warning | third_warning  |
      | 1           | 2           | 5, 14     | 8, 17     | I SMELL A WUMPUS  | BATS NEARBY    | I FEEL A DRAFT |
