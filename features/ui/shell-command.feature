Feature: Shell command launch

  # Shell command launch 001
  Scenario Outline: Shell command launch 001: htw starts a playable game
    Given the project command directory is available on the shell path
    When the player runs shell command <command>
    Then the terminal game starts
    And the output contains prompt <instructions_prompt>
    And the game uses a random seed

    Examples:
      | command | instructions_prompt  |
      | htw     | INSTRUCTIONS (Y-N)?  |

  # Shell command launch 002
  Scenario Outline: Shell command launch 002: normal htw launches use fresh random seeds
    Given the project command directory is available on the shell path
    When the player runs shell command <command> for the first game
    And the player exits before taking a turn
    And the player runs shell command <command> for the second game
    Then the first game seed is different from the second game seed
    And both games have valid placements

    Examples:
      | command |
      | htw     |

  # Shell command launch 003
  Scenario Outline: Shell command launch 003: htw begins with normal game interaction
    Given the project command directory is available on the shell path
    When the player runs shell command <command>
    And the player answers instructions prompt with <answer>
    Then the first turn is displayed
    And the output contains prompt <turn_prompt>

    Examples:
      | command | answer | turn_prompt            |
      | htw     | n      | SHOOT OR MOVE (S-M)?   |
