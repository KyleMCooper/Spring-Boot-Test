/*
 *
 *  (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.steps;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import com.accessofusion.skeleton.config.Constants;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Common methods to interact with APIs
 */
public class BaseStepDef {

  protected ValidatableResponse response;
  protected boolean useTenant = true;
  protected String tenantHeader = Constants.TENANT_HEADER;
  protected String tenantValue = "default";
  protected String versionHeader = "X-VERSION";
  protected String versionValue = "1";
  protected String defaultContentType = MediaType.APPLICATION_JSON_VALUE;

  /**
   * Set the defaults headers to be used for the specified request
   * @param requestSpecification
   * @return
   */
  private RequestSpecification setHeaders(RequestSpecification requestSpecification) {
    if (null != versionHeader && !versionHeader.isEmpty()) {
      requestSpecification.header(versionHeader, versionValue);
    }
    if (null != defaultContentType && !defaultContentType.isEmpty()) {
      requestSpecification.header("Content-Type", defaultContentType);
    }
    if (useTenant) {
      requestSpecification.header(tenantHeader, tenantValue);
    }
    return requestSpecification;
  }

  //Requests
  protected void sendPost(String path, Object data) {
    response = getBaseRequest().body(data).post(path).then();
  }

  protected void sendPatch(String path, Object data) {
    response = getBaseRequest().body(data).patch(path).then();
  }

  protected <T> T sendPostAndExtract(String path, Object data, Class<T> targetClass) {
    sendPost(path, data);
    return response.extract().as(targetClass);
  }

  protected void sendGet(String path) {
    response = getBaseRequest().get(path).then();
  }

  protected void sendGetWithQueryParams(String path, Map<String, ?> queryParams) {
    response = getBaseRequest().queryParams(queryParams).get(path).then();
  }

  protected RequestSpecification getBaseRequest() {
    return setHeaders(with());
  }

  protected void sendGetWithParameters(String templatePath, Object... pathParams) {
    response = getBaseRequest().get(templatePath, pathParams).then();
  }

  protected void sendPut(String path, Object data) {
    response = getBaseRequest().body(data).put(path).then();
  }

  protected void sendPutWithParameters(String path, Object data, Object... pathParams) {
    response = getBaseRequest().body(data).put(path, pathParams).then();
  }

  protected void sendPatchWithParameters(String path, Object data, Object... pathParams) {
    response = getBaseRequest().body(data).patch(path, pathParams).then();
  }

  protected void sendDelete(String path) {
    response = getBaseRequest().delete(path).then();
  }

  protected void validateId() {
    assertNotNull(response);
    response.body("id", notNullValue());
  }

  protected void validateSuccessfulOKResponse() {
    assertNotNull(response);
    response.statusCode(HttpStatus.OK.value());
  }

  protected void validateSuccessfulCreationResponse() {
    assertNotNull(response);
    response.statusCode(HttpStatus.CREATED.value());
  }

  protected void validateStatusCode(HttpStatus status) {
    assertNotNull(response);
    response.statusCode(status.value());
  }
}
