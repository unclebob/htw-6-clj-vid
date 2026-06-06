# mutation-stamp: sha256=73294957692bc86bf1cb1b699cf579bc4358e605d2a8de4a43462a28666e6f73
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T16:42:44Z","feature_name":"Shell command launch","feature_path":"features/ui/shell-command.feature","background_hash":"74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b","implementation_hash":"unknown","scenarios":[{"index":2,"name":"Shell command launch 003: htw begins with normal game interaction","scenario_hash":"a657acaffa7d9ddddd88371a914630f341b0726684ed3acd9be72952b71ca09d","mutation_count":4,"result":{"Total":4,"Killed":4,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:24:08Z"},{"index":0,"name":"Shell command launch 001: htw starts a playable game","scenario_hash":"79275760c032febf60a6182066ae2542c27d71dab2db1d29ac6ffcacd864e2a2","mutation_count":2,"result":{"Total":2,"Killed":2,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:23:35Z"},{"index":1,"name":"Shell command launch 002: normal htw launches use fresh random seeds","scenario_hash":"b4a05ab6d7865780c4278a45c45283179c145ee1242710021e837d0d4f9fc8a7","mutation_count":1,"result":{"Total":1,"Killed":1,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:23:35Z"}]}
# acceptance-mutation-manifest-end

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
        Then the instructions answer is <expected_answer>
        And the first turn is displayed
        And the output contains prompt <turn_prompt>

        Examples:
          | command | answer | expected_answer | turn_prompt            |
          | htw     | n      | n               | SHOOT OR MOVE (S-M)?   |
