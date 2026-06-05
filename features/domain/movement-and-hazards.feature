Feature: Movement and hazard resolution

  Background:
    Given a new Hunt the Wumpus game specification

  # Movement and hazard resolution 001
  Scenario Outline: Movement and hazard resolution 001: legal move changes the player room
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player moves to room <destination_room>
    Then the player is in room <destination_room>
    And the game is still in progress

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms |
      | 1          | 2                | 13          | 14, 15    | 16, 17    |
      | 10         | 11               | 1           | 3, 5      | 7, 13     |

  # Movement and hazard resolution 002
  Scenario Outline: Movement and hazard resolution 002: illegal move is rejected
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player tries to move to room <destination_room>
    Then the move is rejected with message <message>
    And the player is in room <start_room>
    And the game is still in progress

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | message           |
      | 1          | 3                | 13          | 14, 15    | 16, 17    | CAN'T MOVE THERE  |
      | 10         | 15               | 1           | 3, 5      | 7, 13     | CAN'T MOVE THERE  |

  # Movement and hazard resolution 003
  Scenario Outline: Movement and hazard resolution 003: entering a pit loses immediately
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    When the player moves to room <destination_room>
    Then the game is lost
    And the player hears message <message>

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | message                         |
      | 1          | 2                | 13          | 2, 15     | 16, 17    | YYYIIIIEEEE . . . FELL IN PIT  |

  # Movement and hazard resolution 004
  Scenario Outline: Movement and hazard resolution 004: entering a bat room transports the player
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And bat transport will choose room <transport_room>
    When the player moves to room <destination_room>
    Then the player hears message <message>
    And the player is in room <transport_room>
    And the game is still in progress

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | transport_room | message                                             |
      | 1          | 2                | 13          | 14, 15    | 2, 17     | 10             | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!   |

  # Movement and hazard resolution 005
  Scenario Outline: Movement and hazard resolution 005: bat transport can drop the player into a pit
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And bat transport will choose room <transport_room>
    When the player moves to room <destination_room>
    Then the player hears message <bat_message>
    And the player hears message <pit_message>
    And the game is lost

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | transport_room | bat_message                                       | pit_message                      |
      | 1          | 2                | 13          | 10, 15    | 2, 17     | 10             | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! | YYYIIIIEEEE . . . FELL IN PIT   |

  # Movement and hazard resolution 006
  Scenario Outline: Movement and hazard resolution 006: bat transport can drop the player into the Wumpus room
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And bat transport will choose room <transport_room>
    And the Wumpus wake choice is <wake_choice>
    When the player moves to room <destination_room>
    Then the player hears message <bat_message>
    And the Wumpus is in room <expected_wumpus_room>
    And the game is <outcome>

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | transport_room | wake_choice | expected_wumpus_room | outcome      | bat_message                                       |
      | 1          | 2                | 10          | 14, 15    | 2, 17     | 10             | stay        | 10                   | lost         | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! |
      | 1          | 2                | 10          | 14, 15    | 2, 17     | 10             | move to 11  | 11                   | in progress  | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! |
