# mutation-stamp: sha256=afe2b709208200172b29798e15affcb6dc9e2f14154891c4cfa979226f7c38e9
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-05T20:06:14Z","feature_name":"Crooked arrow shooting","feature_path":"features/domain/crooked-arrow.feature","background_hash":"13d117037c0f5f98733b41ac1c2391a100cd6a00f807de23835bb9e0ec63dd85","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Crooked arrow shooting 001: arrow hits the Wumpus along a legal path","scenario_hash":"02951de9617cf64f6bf327e36d9a840d7434143cc00253bedf8b5a8bb6d4632a","mutation_count":42,"result":{"Total":42,"Killed":42,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:58:00Z"},{"index":1,"name":"Crooked arrow shooting 002: invalid arrow segment deviates randomly","scenario_hash":"13975a9f49960b71b6b984be7426768a93fa0c2201e58e2acfd5d7dec4c17034","mutation_count":34,"result":{"Total":34,"Killed":34,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:58:00Z"},{"index":2,"name":"Crooked arrow shooting 003: random deviation can hit the Wumpus","scenario_hash":"124c59ef163deae0cdd1aa2f0ba9f81d88ed46e9421ddebde27f086862addd81","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:58:00Z"},{"index":3,"name":"Crooked arrow shooting 004: arrow path can hit the player","scenario_hash":"06c6ee430357e0da1392caad482b7e475826d6910b78830391ed5d6cf114e506","mutation_count":28,"result":{"Total":28,"Killed":28,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:58:00Z"},{"index":4,"name":"Crooked arrow shooting 005: missed arrow wakes the Wumpus","scenario_hash":"ded52dfb7ee42d007ae16e9371cd50d2be83fb290f6c4aeb3702f8bce493850d","mutation_count":51,"result":{"Total":51,"Killed":51,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:58:00Z"},{"index":5,"name":"Crooked arrow shooting 006: using the last arrow and missing loses","scenario_hash":"473b27e2439658094cab7d1ca1dc3b8ab23146d56833289ef8c104d4aac9d5a7","mutation_count":15,"result":{"Total":15,"Killed":15,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:58:00Z"},{"index":6,"name":"Crooked arrow shooting 007: arrow path length must be one through five rooms","scenario_hash":"1d59cf7518c6e607a14b9977aa24bd23912016d50c67262bb5c8f4ffd21ac281","mutation_count":26,"result":{"Total":26,"Killed":26,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:58:00Z"}]}
# acceptance-mutation-manifest-end

Feature: Crooked arrow shooting

  Background:
    Given a new Hunt the Wumpus game specification

  # Crooked arrow shooting 001
  Scenario Outline: Crooked arrow shooting 001: arrow hits the Wumpus along a legal path
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
    When the player shoots path <path>
    Then the requested path is <expected_path>
    And the arrow visits rooms <visited_rooms>
    And the game is won
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | path           | expected_path  | visited_rooms   | message                  |
      | 1           | 2           | 14, 15    | 16, 17    | 5               | 1                    | 2                    | 14, 15             | 16, 17             | 5                        | 2              | 2              | 2               | AHA! YOU GOT THE WUMPUS! |
      | 1           | 11          | 14, 15    | 16, 17    | 5               | 1                    | 11                   | 14, 15             | 16, 17             | 5                        | 2, 10, 11      | 2, 10, 11      | 2, 10, 11       | AHA! YOU GOT THE WUMPUS! |
      | 6           | 19          | 2, 3      | 4, 5      | 5               | 6                    | 19                   | 2, 3               | 4, 5               | 5                        | 15, 16, 20, 19 | 15, 16, 20, 19 | 15, 16, 20, 19 | AHA! YOU GOT THE WUMPUS! |

  # Crooked arrow shooting 002
  Scenario Outline: Crooked arrow shooting 002: invalid arrow segment deviates randomly
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
    And invalid arrow movement will choose room <deviation_room>
    And the Wumpus wake choice is <wake_choice>
    When the player shoots path <path>
    Then the requested path is <expected_path>
    And the arrow visits rooms <visited_rooms>
    And the player has <remaining_arrows> arrows
    And the game is <outcome>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | path   | expected_path | deviation_room | wake_choice | visited_rooms | remaining_arrows | outcome     |
      | 1           | 13          | 14, 15    | 16, 17    | 5               | 1                    | 13                   | 14, 15             | 16, 17             | 5                        | 3, 4   | 3, 4          | 5              | stay        | 5, 4          | 4                | in progress |
      | 10          | 20          | 2, 3      | 4, 5      | 5               | 10                   | 20                   | 2, 3               | 4, 5               | 5                        | 15, 16 | 15, 16        | 11             | stay        | 11, 12        | 4                | in progress |

  # Crooked arrow shooting 003
  Scenario Outline: Crooked arrow shooting 003: random deviation can hit the Wumpus
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
    And invalid arrow movement will choose room <deviation_room>
    When the player shoots path <path>
    Then the requested path is <expected_path>
    And the arrow visits rooms <visited_rooms>
    And the game is won
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | path | expected_path | deviation_room | visited_rooms | message                  |
      | 1           | 5           | 14, 15    | 16, 17    | 5               | 1                    | 5                    | 14, 15             | 16, 17             | 5                        | 3    | 3             | 5              | 5             | AHA! YOU GOT THE WUMPUS! |

  # Crooked arrow shooting 004
  Scenario Outline: Crooked arrow shooting 004: arrow path can hit the player
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
    When the player shoots path <path>
    Then the requested path is <expected_path>
    And the arrow visits rooms <visited_rooms>
    And the game is lost
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | path        | expected_path | visited_rooms | message               |
      | 1           | 13          | 14, 15    | 16, 17    | 5               | 1                    | 13                   | 14, 15             | 16, 17             | 5                        | 2, 1        | 2, 1          | 2, 1          | OOPS! ARROW GOT YOU! |
      | 1           | 13          | 14, 15    | 16, 17    | 5               | 1                    | 13                   | 14, 15             | 16, 17             | 5                        | 2, 10, 2, 1 | 2, 10, 2, 1   | 2, 10, 2, 1   | OOPS! ARROW GOT YOU! |

  # Crooked arrow shooting 005
  Scenario Outline: Crooked arrow shooting 005: missed arrow wakes the Wumpus
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
    And the Wumpus wake choice is <wake_choice>
    When the player shoots path <path>
    Then the requested path is <expected_path>
    And the arrow visits rooms <visited_rooms>
    And the player has <remaining_arrows> arrows
    And the Wumpus is in room <final_wumpus_room>
    And the game is <outcome>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | wake_choice | path | expected_path | visited_rooms | remaining_arrows | final_wumpus_room | outcome     |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | 1                    | 10                   | 14, 15             | 16, 17             | 5                        | stay        | 5    | 5             | 5             | 4                | 10                | in progress |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | 1                    | 10                   | 14, 15             | 16, 17             | 5                        | move to 2   | 5    | 5             | 5             | 4                | 2                 | in progress |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | 1                    | 10                   | 14, 15             | 16, 17             | 5                        | move to 1   | 5    | 5             | 5             | 4                | 1                 | lost        |

  # Crooked arrow shooting 006
  Scenario Outline: Crooked arrow shooting 006: using the last arrow and missing loses
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
    And the Wumpus wake choice is <wake_choice>
    When the player shoots path <path>
    Then the requested path is <expected_path>
    And the player has <remaining_arrows> arrows
    And the game is lost
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | wake_choice | path | expected_path | remaining_arrows | message               |
      | 1           | 10          | 14, 15    | 16, 17    | 1               | 1                    | 10                   | 14, 15             | 16, 17             | 1                        | stay        | 5    | 5             | 0                | YOU RAN OUT OF ARROWS |

  # Crooked arrow shooting 007
  Scenario Outline: Crooked arrow shooting 007: arrow path length must be one through five rooms
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>, arrows <expected_starting_arrows>
    When the player tries to shoot path <path>
    Then the requested path is <expected_path>
    And the shot is rejected with message <message>
    And the player has <starting_arrows> arrows
    And the game is still in progress

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | expected_player_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | expected_starting_arrows | path                   | expected_path          | message           |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | 1                    | 10                   | 14, 15             | 16, 17             | 5                        | none                   | none                   | CAN'T SHOOT THERE |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | 1                    | 10                   | 14, 15             | 16, 17             | 5                        | 2, 10, 11, 12, 13, 14 | 2, 10, 11, 12, 13, 14 | CAN'T SHOOT THERE |
