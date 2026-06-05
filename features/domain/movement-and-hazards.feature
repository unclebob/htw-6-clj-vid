# mutation-stamp: sha256=45fafe400cf34c000ac8e1c64a54179e5e2686d7142f37431a0490ac641a4796
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-05T20:26:36Z","feature_name":"Movement and hazard resolution","feature_path":"features/domain/movement-and-hazards.feature","background_hash":"13d117037c0f5f98733b41ac1c2391a100cd6a00f807de23835bb9e0ec63dd85","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Movement and hazard resolution 001: legal move changes the player room","scenario_hash":"d6b09df0e7c6beb737a5bc5673567c32c231e48419298a6a23d022d771f673e3","mutation_count":20,"result":{"Total":20,"Killed":20,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:01:57Z"},{"index":1,"name":"Movement and hazard resolution 002: illegal move is rejected","scenario_hash":"af7059b5a81b03f4f477a9af013971aa5eae7765e95180da9860ea7677873ff8","mutation_count":22,"result":{"Total":22,"Killed":22,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:01:57Z"},{"index":2,"name":"Movement and hazard resolution 003: entering a pit loses immediately","scenario_hash":"29fb5524724a9c3afcd495543ba9a196cd10d7c89da1db71013e21c5d1d027cb","mutation_count":11,"result":{"Total":11,"Killed":11,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:01:57Z"},{"index":3,"name":"Movement and hazard resolution 004: entering a bat room transports the player","scenario_hash":"3ba7c362672cbbc5771eb80b4d82307af9b914a038a2c96240f747b4b2bed215","mutation_count":13,"result":{"Total":13,"Killed":13,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:01:57Z"},{"index":4,"name":"Movement and hazard resolution 005: bat transport can drop the player into a pit","scenario_hash":"ddb5b087aa7cd3376e3fe2db4a75d56a74967aeffc3ffdb25716f2034179c035","mutation_count":13,"result":{"Total":13,"Killed":13,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:01:57Z"},{"index":5,"name":"Movement and hazard resolution 006: bat transport can drop the player into the Wumpus room","scenario_hash":"89ef6b23840c930f8ac4ce5e6b9958b0feac87dab3e86d407bf544296d641ea0","mutation_count":30,"result":{"Total":30,"Killed":30,"Survived":0,"Errors":0},"tested_at":"2026-06-05T20:01:57Z"}]}
# acceptance-mutation-manifest-end

Feature: Movement and hazard resolution

  Background:
    Given a new Hunt the Wumpus game specification

  # Movement and hazard resolution 001
  Scenario Outline: Movement and hazard resolution 001: legal move changes the player room
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    When the player moves to room <destination_room>
    Then the requested move is <expected_destination_room>
    And the player is in room <expected_destination_room>
    And the game is still in progress

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_destination_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms |
      | 1          | 2                | 13          | 14, 15    | 16, 17    | 1                    | 2                         | 13                   | 14, 15             | 16, 17             |
      | 10         | 11               | 1           | 3, 5      | 7, 13     | 10                   | 11                        | 1                    | 3, 5               | 7, 13              |

  # Movement and hazard resolution 002
  Scenario Outline: Movement and hazard resolution 002: illegal move is rejected
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    When the player tries to move to room <destination_room>
    Then the requested move is <expected_destination_room>
    And the move is rejected with message <message>
    And the player is in room <expected_player_room>
    And the game is still in progress

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_destination_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | message          |
      | 1          | 3                | 13          | 14, 15    | 16, 17    | 1                    | 3                         | 13                   | 14, 15             | 16, 17             | CAN'T MOVE THERE |
      | 10         | 15               | 1           | 3, 5      | 7, 13     | 10                   | 15                        | 1                    | 3, 5               | 7, 13              | CAN'T MOVE THERE |

  # Movement and hazard resolution 003
  Scenario Outline: Movement and hazard resolution 003: entering a pit loses immediately
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    When the player moves to room <destination_room>
    Then the requested move is <expected_destination_room>
    And the game is lost
    And the player hears message <message>

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_destination_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | message                        |
      | 1          | 2                | 13          | 2, 15     | 16, 17    | 1                    | 2                         | 13                   | 2, 15              | 16, 17             | YYYIIIIEEEE . . . FELL IN PIT |

  # Movement and hazard resolution 004
  Scenario Outline: Movement and hazard resolution 004: entering a bat room transports the player
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    And bat transport will choose room <transport_room>
    When the player moves to room <destination_room>
    Then the requested move is <expected_destination_room>
    And the player hears message <message>
    And the player is in room <expected_transport_room>
    And the game is still in progress

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_destination_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | transport_room | expected_transport_room | message                                           |
      | 1          | 2                | 13          | 14, 15    | 2, 17     | 1                    | 2                         | 13                   | 14, 15             | 2, 17               | 10             | 10                      | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! |

  # Movement and hazard resolution 005
  Scenario Outline: Movement and hazard resolution 005: bat transport can drop the player into a pit
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <expected_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    And bat transport will choose room <transport_room>
    When the player moves to room <destination_room>
    Then the requested move is <expected_destination_room>
    And the player hears message <bat_message>
    And the player hears message <pit_message>
    And the game is lost

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_destination_room | expected_wumpus_room | expected_pit_rooms | expected_bat_rooms | transport_room | bat_message                                       | pit_message                     |
      | 1          | 2                | 13          | 10, 15    | 2, 17     | 1                    | 2                         | 13                   | 10, 15             | 2, 17               | 10             | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! | YYYIIIIEEEE . . . FELL IN PIT |

  # Movement and hazard resolution 006
  Scenario Outline: Movement and hazard resolution 006: bat transport can drop the player into the Wumpus room
    Given a game has the player in room <start_room>
    And the Wumpus is in room <wumpus_room>
    And pits are in rooms <pit_rooms>
    And bats are in rooms <bat_rooms>
    And the configured setup is player <expected_player_room>, Wumpus <setup_wumpus_room>, pits <expected_pit_rooms>, bats <expected_bat_rooms>
    And bat transport will choose room <transport_room>
    And the Wumpus wake choice is <wake_choice>
    When the player moves to room <destination_room>
    Then the requested move is <expected_destination_room>
    And the player hears message <bat_message>
    And the Wumpus is in room <expected_wumpus_room>
    And the game is <outcome>

    Examples:
      | start_room | destination_room | wumpus_room | pit_rooms | bat_rooms | expected_player_room | expected_destination_room | setup_wumpus_room | expected_pit_rooms | expected_bat_rooms | transport_room | wake_choice | expected_wumpus_room | outcome     | bat_message                                       |
      | 1          | 2                | 10          | 14, 15    | 2, 17     | 1                    | 2                         | 10                | 14, 15             | 2, 17               | 10             | stay        | 10                   | lost        | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! |
      | 1          | 2                | 10          | 14, 15    | 2, 17     | 1                    | 2                         | 10                | 14, 15             | 2, 17               | 10             | move to 11  | 11                   | in progress | ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU! |
