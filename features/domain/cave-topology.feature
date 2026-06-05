Feature: Cave topology

  Background:
    Given a new Hunt the Wumpus game specification

  # Cave topology 001
  Scenario Outline: Cave topology 001: canonical room exits
    When the cave topology is inspected
    Then room <room> has exits <exits>

    Examples:
      | room | exits     |
      | 1    | 2, 5, 8   |
      | 2    | 1, 3, 10  |
      | 3    | 2, 4, 12  |
      | 4    | 3, 5, 14  |
      | 5    | 1, 4, 6   |
      | 6    | 5, 7, 15  |
      | 7    | 6, 8, 17  |
      | 8    | 1, 7, 9   |
      | 9    | 8, 10, 18 |
      | 10   | 2, 9, 11  |
      | 11   | 10, 12, 19 |
      | 12   | 3, 11, 13 |
      | 13   | 12, 14, 20 |
      | 14   | 4, 13, 15 |
      | 15   | 6, 14, 16 |
      | 16   | 15, 17, 20 |
      | 17   | 7, 16, 18 |
      | 18   | 9, 17, 19 |
      | 19   | 11, 18, 20 |
      | 20   | 13, 16, 19 |

  # Cave topology 002
  Scenario: Cave topology 002: all rooms have three tunnels
    When the cave topology is inspected
    Then the cave contains 20 rooms
    And every room has 3 exits

  # Cave topology 003
  Scenario: Cave topology 003: tunnels are bidirectional
    When the cave topology is inspected
    Then every tunnel has a matching return tunnel

  # Cave topology 004
  Scenario: Cave topology 004: rooms never exit to themselves
    When the cave topology is inspected
    Then no room has an exit to itself

  # Cave topology 005
  Scenario: Cave topology 005: every room is reachable
    When the cave topology is inspected
    Then every room is reachable from room 1
