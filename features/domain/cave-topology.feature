# mutation-stamp: sha256=a916b9e95e16088b2a7ffd0e327bfd4238554eef13500dde1ea9d8c0d338c16d
# acceptance-mutation-manifest-begin
# {"version":1,"tested_at":"2026-06-06T16:56:43Z","feature_name":"Cave topology","feature_path":"features/domain/cave-topology.feature","background_hash":"13d117037c0f5f98733b41ac1c2391a100cd6a00f807de23835bb9e0ec63dd85","implementation_hash":"unknown","scenarios":[{"index":0,"name":"Cave topology 001: canonical room exits","scenario_hash":"c998bd95707faf7304a4cbadfce3213f5d940b2d2f7d742c35c86b62bbc54d9a","mutation_count":40,"result":{"Total":40,"Killed":40,"Survived":0,"Errors":0},"tested_at":"2026-06-05T19:39:51Z"}]}
# acceptance-mutation-manifest-end

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
