Feature: SkeletonResource management

  Scenario Outline: Create skeletonResources and performing modifications
    Given that as a user I want to create a single skeletonResource
    When I send a skeletonResource creation request using id "<id>", name "<name>" and status "<status>"
    Then the service returns the created skeletonResource!
    And I can ask for the skeletonResource using the returned id
    And if I change the skeletonResource name to "<newName>" then the get request will answer a name different to "<name>"
    And finally if I want to update only the active status, I can patch the skeletonResource
    Then skeletonResource status is not "<status>"
    Examples:
      | id | name   | status | newName        |
      | 1  | A Test | true   | A Test updated |
      | 2  | B Test | false  | B Test updated |

  Scenario: Paging through the skeletonResource records
    Given that exists at least 30 skeletonResources in the back end data source
    When I send a request to list the skeletonResources
    Then I can navigate through the skeletonResources using pagination
    And also I can sort the skeletonResources by name descending

  Scenario: Check the service validations to avoid skeletonResource duplicates
    Given that as a user I want to create a single skeletonResource
    When I send two skeletonResource requests with the same name
    Then I receive a skeletonResource duplication message

  Scenario: Check the service validations for skeletonResources creation and updates
    Given that as a user I want to create a single skeletonResource
    When I forgot to send the skeletonResource name
    Then I receive an error telling me the related problem for the skeletonResource field "name" with details "Name is empty or it only contains whitespaces"

  Scenario: Check the service validations for skeletonResources patching
    Given that as a user I want to patch a single skeletonResource
    When I send a malformed skeletonResource name using only whitespaces
    Then I receive an error telling me the related problem for the skeletonResource field "name" with details "Name is empty or it only contains whitespaces"