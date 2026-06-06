Feature: Shell random event choices

  Background:
    Given the project command directory is available on the shell path

  # Shell random event choices 001
  Scenario Outline: Shell random event choices 001: htw randomizes unforced bat transport choice
    Given htw is launched with fixed setup <setup_options>
    And no bat transport override is supplied
    When QA plays the htw command with seeds <seeds> and terminal input <terminal_input>
    Then the observed bat transport rooms are within <legal_transport_rooms>
    And at least <minimum_distinct_rooms> distinct bat transport rooms are observed
    And the observed bat transport rooms are not always <first_legal_room>

    Examples:
      | setup_options                                    | seeds                         | terminal_input | legal_transport_rooms                         | minimum_distinct_rooms | first_legal_room |
      | --player 1 --wumpus 13 --pits 14,15 --bats 2,17 | 51,52,53,54,55,56,57,58      | n; m 2         | 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20 | 2 | 1 |

  # Shell random event choices 002
  Scenario Outline: Shell random event choices 002: htw randomizes unforced Wumpus wake choice
    Given htw is launched with fixed setup <setup_options>
    And no Wumpus wake override is supplied
    When QA plays the htw command with seeds <seeds> and terminal input <terminal_input>
    Then the observed Wumpus wake rooms are within <legal_wake_rooms>
    And at least <minimum_distinct_rooms> distinct Wumpus wake rooms are observed
    And the observed Wumpus wake rooms are not always <first_legal_room>

    Examples:
      | setup_options                                    | seeds                         | terminal_input | legal_wake_rooms | minimum_distinct_rooms | first_legal_room |
      | --player 1 --wumpus 2 --pits 14,15 --bats 16,17 | 101,102,103,104,105,106,107,108 | n; m 2         | 2, 1, 3, 10     | 2                      | 2                |

  # Shell random event choices 003
  Scenario Outline: Shell random event choices 003: htw randomizes unforced arrow fallback choice
    Given htw is launched with fixed setup <setup_options>
    And no arrow deviation override is supplied
    When QA plays the htw command with seeds <seeds> and terminal input <terminal_input>
    Then the observed arrow fallback rooms are within <legal_fallback_rooms>
    And at least <minimum_distinct_rooms> distinct arrow fallback rooms are observed
    And the observed arrow fallback rooms are not always <first_legal_room>

    Examples:
      | setup_options                                    | seeds                         | terminal_input | legal_fallback_rooms | minimum_distinct_rooms | first_legal_room |
      | --player 1 --wumpus 13 --pits 14,15 --bats 16,17 | 201,202,203,204,205,206,207,208 | n; s 20        | 2, 5, 8              | 2                      | 2                |
