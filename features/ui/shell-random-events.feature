# mutation-stamp: sha256=3f85d60eb037ee3d0b8b9bac9e4737e5c01b103e8430abb4103489007711c687
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T17:22:37Z","feature_name":"Shell random event choices","feature_path":"features/ui/shell-random-events.feature","background_hash":"63ed2ed2bb744e8f29bddb6d5d014b6a42f18c0382ab1b162be85e56b9f5882a","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Shell random event choices 001: htw randomizes unforced bat transport choice","scenario_hash":"5b645313fba26cbce2511253d9def978ec4e1483d8fd72dac71d922c12c084f2","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-06-06T17:19:29Z"},{"index":1,"name":"Shell random event choices 002: htw randomizes unforced Wumpus wake choice","scenario_hash":"e91365e6a63003647eeac5dbf5865e999746c839254cbb5a638edc22e09dcf07","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-06-06T17:19:29Z"},{"index":2,"name":"Shell random event choices 003: htw randomizes unforced arrow fallback choice","scenario_hash":"f870048c1a82ab226da28d9ed3ebbc1df4fe6edb74576f48504c9ee2a7fba5f3","mutation_count":6,"result":{"Total":6,"Killed":6,"Survived":0,"Errors":0},"tested_at":"2026-06-06T17:19:29Z"}]}
# acceptance-mutation-manifest-end

Feature: Shell random event choices

  Background:
    Given the project command directory is available on the shell path

  # Shell random event choices 001
  Scenario Outline: Shell random event choices 001: htw randomizes unforced bat transport choice
    Given htw is launched with fixed setup <setup_options>
    And no bat transport override is supplied
    When QA plays the htw command with seeds <seeds> and terminal input <terminal_input>
    Then the observed run seeds are <expected_seeds>
    And the observed random rooms are <observed_rooms>
    And exactly <distinct_room_count> distinct random rooms are observed

    Examples:
      | setup_options                                                     | seeds                    | expected_seeds          | terminal_input | observed_rooms        | distinct_room_count |
      | --player 1 --wumpus 13 --pits 14,15 --bats 2,17 --show-seed true | 51,52,53,54,55,56,57,58 | 51,52,53,54,55,56,57,58 | n; m 2         | 20,14,3,7,1,13,9,12 | 8                   |

  # Shell random event choices 002
  Scenario Outline: Shell random event choices 002: htw randomizes unforced Wumpus wake choice
    Given htw is launched with fixed setup <setup_options>
    And no Wumpus wake override is supplied
    When QA plays the htw command with seeds <seeds> and terminal input <terminal_input>
    Then the observed run seeds are <expected_seeds>
    And the observed random rooms are <observed_rooms>
    And exactly <distinct_room_count> distinct random rooms are observed

    Examples:
      | setup_options                                                     | seeds                           | expected_seeds                   | terminal_input | observed_rooms        | distinct_room_count |
      | --player 1 --wumpus 2 --pits 14,15 --bats 16,17 --show-seed true | 101,102,103,104,105,106,107,108 | 101,102,103,104,105,106,107,108 | n; m 2         | 10,3,1,10,10,10,3,1 | 3                   |

  # Shell random event choices 003
  Scenario Outline: Shell random event choices 003: htw randomizes unforced arrow fallback choice
    Given htw is launched with fixed setup <setup_options>
    And no arrow deviation override is supplied
    When QA plays the htw command with seeds <seeds> and terminal input <terminal_input>
    Then the observed run seeds are <expected_seeds>
    And the observed random rooms are <observed_rooms>
    And exactly <distinct_room_count> distinct random rooms are observed

    Examples:
      | setup_options                                                      | seeds                           | expected_seeds                   | terminal_input | observed_rooms  | distinct_room_count |
      | --player 1 --wumpus 13 --pits 14,15 --bats 16,17 --show-seed true | 201,202,203,204,205,206,207,208 | 201,202,203,204,205,206,207,208 | n; s 20        | 8,8,8,5,5,8,8,8 | 2                   |
