Feature: Reproducible setup

  # Reproducible setup 001
  Scenario Outline: Reproducible setup 001: same seed produces identical placement
    When a game is started with seed <seed>
    And a second game is started with seed <seed>
    Then both games have the same player room
    And both games have the same Wumpus room
    And both games have the same pit rooms
    And both games have the same bat rooms

    Examples:
      | seed |
      | 1973 |
      | 1975 |
      | 1976 |

  # Reproducible setup 002
  Scenario: Reproducible setup 002: same setup reuse preserves placement
    Given a game was started with seed 1973
    When the same setup is reused
    Then the reused setup has the same player room
    And the reused setup has the same Wumpus room
    And the reused setup has the same pit rooms
    And the reused setup has the same bat rooms
