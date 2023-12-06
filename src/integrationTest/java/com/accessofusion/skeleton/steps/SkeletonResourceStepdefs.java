package com.accessofusion.skeleton.steps;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;
import org.zalando.problem.violations.Violation;

public class SkeletonResourceStepdefs extends BaseStepDef {

  private static final String SKELETON_RESOURCE_TEST_ID = "1";
  private static final String JSON_PATH_FOR_SKELETON_RESOURCE_PAGE = "_embedded.skeletonResources";
  private static final String SKELETON_RESOURCE_ID_PATH = "/{skeletonResourceId}";
  private static final String SKELETON_RESOURCE_BASE_PATH = "skeletonResource";
  private SkeletonResource currentSkeletonResource;

  /**
   * Begin the skeletonResource creation process. Depends on: api.create
   */
  @Given("that as a user I want to create a single skeletonResource")
  public void thatAsAUserIWantToCreateASingleSkeletonResource() {
    currentSkeletonResource = new SkeletonResource();
  }

  /**
   * Sends an object to the api and validates the result. Depends on: api.create
   */
  @When("I send a skeletonResource creation request using id {string}, name {string} and status {string}")
  public void iSendACreationRequestUsingIdNameAndStatus(String id, String name, String status) {
    currentSkeletonResource.setId(id);
    currentSkeletonResource.setName(name);
    currentSkeletonResource.setActive(Boolean.valueOf(status));
    sendPost(SKELETON_RESOURCE_BASE_PATH, currentSkeletonResource);
    validateId();
    validateSuccessfulCreationResponse();
  }

  /**
   * Check if created object response is valid. Depends on: api.create
   */
  @Then("the service returns the created skeletonResource!")
  public void theServiceReturnTheCreatedObject() {
    currentSkeletonResource = response.extract().as(SkeletonResource.class);
  }

  /**
   * Check if object was already created in datasource. Depends on: api.create (For readonly you
   * probably will populate fields manually)
   */
  @And("I can ask for the skeletonResource using the returned id")
  public void iCanAskForTheSkeletonResourceUsingTheReturnedId() {
    sendGetWithParameters(
        SKELETON_RESOURCE_BASE_PATH + SKELETON_RESOURCE_ID_PATH,
        currentSkeletonResource.getId());
    validateId();
    validateSuccessfulOKResponse();
    SkeletonResource fetchedResource = response.extract().as(SkeletonResource.class);
    assertEquals(currentSkeletonResource.getId(), fetchedResource.getId());
  }

  /**
   * Check if object can be updated. Depends on api.create  and api.update as this udpate will
   * require data added with create and retrieved with get
   */
  @And("if I change the skeletonResource name to {string} then the get request will answer a name different to {string}")
  public void ifIChangeTheNameToThenTheGetRequestWillAnswerANameDifferentTo(String newName,
      String oldName) {
    currentSkeletonResource.setName(newName);
    sendPutWithParameters(
        SKELETON_RESOURCE_BASE_PATH + SKELETON_RESOURCE_ID_PATH,
        currentSkeletonResource,
        currentSkeletonResource.getId());
    validateId();
    validateSuccessfulOKResponse();
    currentSkeletonResource = response.extract().as(SkeletonResource.class);
    assertNotEquals(oldName, currentSkeletonResource.getName());
  }

  /**
   * Check if object can be patched. Depends on api.create and api.patch as this patch will require data created
   * on create and retrieved with get
   */
  @And("finally if I want to update only the active status, I can patch the skeletonResource")
  public void finallyIfIWantToUpdateOnlyTheActiveStatusICanPatchTheSkeletonResource() {
    SkeletonResource patch = new SkeletonResource();
    //Change the current status
    patch.setActive(!currentSkeletonResource.getActive());
    sendPatchWithParameters(
        SKELETON_RESOURCE_BASE_PATH + SKELETON_RESOURCE_ID_PATH,
        patch,
        currentSkeletonResource.getId());
    validateId();
    validateSuccessfulOKResponse();
    SkeletonResource fetched = response.extract().as(SkeletonResource.class);
    assertNotEquals(currentSkeletonResource.getActive(), fetched.getActive());
    currentSkeletonResource = fetched;
  }

  /**
   * Check if object was patched. Depends on api.create and api.patch as this patch will require data created on
   * create and retrieved with get
   */
  @Then("skeletonResource status is not {string}")
  public void statusIsNot(String status) {
    assertNotEquals(Boolean.valueOf(status), currentSkeletonResource.getActive());
  }

  /**
   * Create a list of resources  to test pagination. Depends on api.create
   */
  @Given("that exists at least {int} skeletonResources in the back end data source")
  public void thatExistsAtLeastSkeletonResourcesInTheBackEndDataSource(
      int skeletonResourcesToCreate) {
    SkeletonResource skeletonResource = new SkeletonResource();
    Random random = new Random();
    for (int i = 0; i < skeletonResourcesToCreate; i++) {
      skeletonResource.setId(Integer.toString(i));
      skeletonResource.setName("A" + (i % 25) + RandomStringUtils.randomAlphanumeric(10));
      skeletonResource.setActive(random.nextBoolean());
      sendPost(SKELETON_RESOURCE_BASE_PATH, skeletonResource);
    }
  }

  /**
   * Send a query with pagination. Depends on api.create to create the resources
   */
  @When("I send a request to list the skeletonResources")
  public void iSendARequestToListTheSkeletonResources() {
    //Request in pages of 5 elements ordered by name ASC
    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put("size", 5);
    queryParams.put("page", 0);
    queryParams.put("sort", "name,ASC");
    sendGetWithQueryParams(SKELETON_RESOURCE_BASE_PATH, queryParams);
  }

  /**
   * Navigates over the resources using pagination. Depends on api.create to create the resources
   */
  @Then("I can navigate through the skeletonResources using pagination")
  public void iCanNavigateThroughTheResultsUsingPagination() {
    List<SkeletonResource> skeletonResources = response.extract().body().jsonPath()
        .getList(JSON_PATH_FOR_SKELETON_RESOURCE_PAGE, SkeletonResource.class);
    int totalElements = response.extract().body().jsonPath().getInt("page.totalElements");
    assertNotNull(skeletonResources);
    assertThat(totalElements, Matchers.greaterThanOrEqualTo(skeletonResources.size()));

    String nextPage = response.extract().body().jsonPath().getString("_links.next.href");

    sendGet(nextPage);
    List<SkeletonResource> skeletonResourcesNext = response.extract().body().jsonPath()
        .getList(JSON_PATH_FOR_SKELETON_RESOURCE_PAGE, SkeletonResource.class);
    assertNotNull(skeletonResources);
    assertNotEquals(
        skeletonResources.stream().map(SkeletonResource::getId).collect(Collectors.joining()),
        skeletonResourcesNext.stream().map(SkeletonResource::getId).collect(Collectors.joining()));
  }

  /**
   * Navigates over the resources using pagination and sorting. Depends on api.create to create the
   * resources
   */
  @And("also I can sort the skeletonResources by name descending")
  public void alsoICanSortTheRecordsByNameDescending() {
    //From response in previous step we'll setup this test
    String lastPage = response.extract().body().jsonPath().getString("_links.last.href");
    sendGet(lastPage);
    List<SkeletonResource> skeletonResourcesLast = response.extract().body().jsonPath()
        .getList(JSON_PATH_FOR_SKELETON_RESOURCE_PAGE, SkeletonResource.class);
    SkeletonResource lastSkeletonResource = skeletonResourcesLast
        .get(skeletonResourcesLast.size() - 1);
    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put("size", 5);
    queryParams.put("page", 0);
    queryParams.put("sort", "name,desc");
    sendGetWithQueryParams(SKELETON_RESOURCE_BASE_PATH, queryParams);
    List<SkeletonResource> skeletonResources = response.extract().body().jsonPath()
        .getList(JSON_PATH_FOR_SKELETON_RESOURCE_PAGE, SkeletonResource.class);
    SkeletonResource firstSkeletonResource = skeletonResources.get(0);
    //lastSkeletonResource from sorted list in ascending order should be the first in descending order
    assertEquals(firstSkeletonResource.getId(), lastSkeletonResource.getId());
  }

  /**
   * Send a bad request. Depends on api.create to test validations
   */
  @When("I forgot to send the skeletonResource name")
  public void iForgotToSendTheName() {
    currentSkeletonResource.setId(SKELETON_RESOURCE_TEST_ID);
    currentSkeletonResource.setActive(false);
    sendPost(SKELETON_RESOURCE_BASE_PATH, currentSkeletonResource);
  }

  /**
   * Validates the expected bad request response. Depends on api.create to test validations
   */
  @Then("I receive an error telling me the related problem for the skeletonResource field {string} with details {string}")
  public void iReceiveAnErrorTellingMeTheRelatedProblem(String field, String expectedMessage) {
    validateStatusCode(HttpStatus.BAD_REQUEST);
    List<Violation> violations = response.extract().jsonPath()
        .getList("violations", Violation.class);
    assertNotNull(violations);
    assertThat(violations.size(), greaterThan(0));
    assertTrue(String
            .format("Message or field not found in constraint violations. First entry: %s - %s",
                violations.get(0).getField(), violations.get(0).getMessage()),
        violations.stream().anyMatch(
            violation -> field.equals(violation.getField()) && expectedMessage
                .equals(violation.getMessage())));
  }

  /**
   * Send a bad patch request. Depends on api.patch to test validations
   */
  @Given("that as a user I want to patch a single skeletonResource")
  public void thatAsAUserIWantToPatchASingleSkeletonResource() {
    currentSkeletonResource = new SkeletonResource();
    currentSkeletonResource.setName("                   ");
  }

  /**
   * Validate the bad patch response. Depends on api.patch to test validations
   */
  @When("I send a malformed skeletonResource name using only whitespaces")
  public void iSendAMalformedNameUsingOnlyWhitespaces() {
    sendPatchWithParameters(
        SKELETON_RESOURCE_BASE_PATH + SKELETON_RESOURCE_ID_PATH,
        currentSkeletonResource, SKELETON_RESOURCE_TEST_ID);
  }

  /**
   * Send a request with the same name twice. Depends on api.create to test validations
   */
  @When("I send two skeletonResource requests with the same name")
  public void iSendTheSameRequestTwice() {
    currentSkeletonResource.setId(UUID.randomUUID().toString());
    currentSkeletonResource.setName("test name");
    currentSkeletonResource.setActive(true);
    sendPost(SKELETON_RESOURCE_BASE_PATH, currentSkeletonResource);
    validateId();
    validateSuccessfulCreationResponse();
    //Set a new id without changing the name
    currentSkeletonResource.setId(UUID.randomUUID().toString());
    sendPost(SKELETON_RESOURCE_BASE_PATH, currentSkeletonResource);
  }

  /**
   * Validate the duplication message . Depends on api.create to test validations
   */
  @Then("I receive a skeletonResource duplication message")
  public void iReceiveASkeletonResourceDuplication() {
    validateStatusCode(HttpStatus.CONFLICT);
    response.assertThat().body("detail", containsString(ErrorMessages.DUPLICATE_KEY_EXCEPTION));
  }
}
