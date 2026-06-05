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
    When the player shoots path <path>
    Then the arrow visits rooms <visited_rooms>
    And the game is won
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | path       | visited_rooms | message                                           |
      | 1           | 2           | 14, 15    | 16, 17    | 5               | 2          | 2             | AHA! YOU GOT THE WUMPUS!                         |
      | 1           | 11          | 14, 15    | 16, 17    | 5               | 2, 10, 11  | 2, 10, 11     | AHA! YOU GOT THE WUMPUS!                         |
      | 6           | 19          | 2, 3      | 4, 5      | 5               | 15, 16, 20, 19 | 15, 16, 20, 19 | AHA! YOU GOT THE WUMPUS!                      |

  # Crooked arrow shooting 002
  Scenario Outline: Crooked arrow shooting 002: invalid arrow segment deviates randomly
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And invalid arrow movement will choose room <deviation_room>
    And the Wumpus wake choice is <wake_choice>
    When the player shoots path <path>
    Then the arrow visits rooms <visited_rooms>
    And the player has <remaining_arrows> arrows
    And the game is <outcome>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | path       | deviation_room | wake_choice | visited_rooms | remaining_arrows | outcome      |
      | 1           | 13          | 14, 15    | 16, 17    | 5               | 3, 4       | 5              | stay        | 5, 4          | 4                | in progress  |
      | 10          | 20          | 2, 3      | 4, 5      | 5               | 15, 16     | 11             | stay        | 11, 12        | 4                | in progress  |

  # Crooked arrow shooting 003
  Scenario Outline: Crooked arrow shooting 003: random deviation can hit the Wumpus
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And invalid arrow movement will choose room <deviation_room>
    When the player shoots path <path>
    Then the arrow visits rooms <visited_rooms>
    And the game is won
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | path    | deviation_room | visited_rooms | message                   |
      | 1           | 5           | 14, 15    | 16, 17    | 5               | 3      | 5              | 5             | AHA! YOU GOT THE WUMPUS!  |

  # Crooked arrow shooting 004
  Scenario Outline: Crooked arrow shooting 004: arrow path can hit the player
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    When the player shoots path <path>
    Then the arrow visits rooms <visited_rooms>
    And the game is lost
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | path          | visited_rooms | message                 |
      | 1           | 13          | 14, 15    | 16, 17    | 5               | 2, 1          | 2, 1          | OOPS! ARROW GOT YOU!   |
      | 1           | 13          | 14, 15    | 16, 17    | 5               | 2, 10, 2, 1   | 2, 10, 2, 1   | OOPS! ARROW GOT YOU!   |

  # Crooked arrow shooting 005
  Scenario Outline: Crooked arrow shooting 005: missed arrow wakes the Wumpus
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the Wumpus wake choice is <wake_choice>
    When the player shoots path <path>
    Then the arrow visits rooms <visited_rooms>
    And the player has <remaining_arrows> arrows
    And the Wumpus is in room <expected_wumpus_room>
    And the game is <outcome>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | wake_choice | path | visited_rooms | remaining_arrows | expected_wumpus_room | outcome      |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | stay        | 5    | 5             | 4                | 10                   | in progress  |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | move to 2   | 5    | 5             | 4                | 2                    | in progress  |
      | 1           | 10          | 14, 15    | 16, 17    | 5               | move to 1   | 5    | 5             | 4                | 1                    | lost         |

  # Crooked arrow shooting 006
  Scenario Outline: Crooked arrow shooting 006: using the last arrow and missing loses
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the player has <starting_arrows> arrows
    And the Wumpus wake choice is <wake_choice>
    When the player shoots path <path>
    Then the player has <remaining_arrows> arrows
    And the game is lost
    And the player hears message <message>

    Examples:
      | player_room | wumpus_room | pit_rooms | bat_rooms | starting_arrows | wake_choice | path | remaining_arrows | message              |
      | 1           | 10          | 14, 15    | 16, 17    | 1               | stay        | 5    | 0                | YOU RAN OUT OF ARROWS |

  # Crooked arrow shooting 007
  Scenario Outline: Crooked arrow shooting 007: arrow path length must be one through five rooms
    Given a game has the player in room <player_room>
    And the Wumpus is in room <wumpus_room>
    And the player has <starting_arrows> arrows
    When the player tries to shoot path <path>
    Then the shot is rejected with message <message>
    And the player has <starting_arrows> arrows
    And the game is still in progress

    Examples:
      | player_room | wumpus_room | starting_arrows | path             | message            |
      | 1           | 10          | 5               | none             | CAN'T SHOOT THERE  |
      | 1           | 10          | 5               | 2, 10, 11, 12, 13, 14 | CAN'T SHOOT THERE |
