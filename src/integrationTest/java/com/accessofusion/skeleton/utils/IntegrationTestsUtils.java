/*
 * *
 *  * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.utils;

public class IntegrationTestsUtils {

  private static final String CONTAINER_IMAGE_TAG_PROPERTY = "integration_image_tag";
  private static final String MISSING_PROPERTY_EXCEPTION_FORMAT = "The %s system property is required.";

  /**
   * Returns the container image tag to be used in the integration tests
   */
  public static String getContainerImageTag() {
    return resolveSystemOrEnvironmentProperty(CONTAINER_IMAGE_TAG_PROPERTY, "couchbase:community-7.2.0", true);
  }

  /**
   * Returns the string value of a system or environment property. System properties will have
   * priority.
   *
   * @param property property name
   * @param defaultValue default value to be return if the property is missing. It will be ignored
   * if failOnMissingProperty is set to true
   * @param failOnMissingProperty raise an exception if the property is not present
   */
  public static String resolveSystemOrEnvironmentProperty(String property, String defaultValue,
      boolean failOnMissingProperty) {
    if (null != System.getProperty(property)) {
      return System.getProperty(property);
    }
    if (null != System.getenv(property)) {
      return System.getenv(property);
    }

    if (failOnMissingProperty) {
      throw new RuntimeException(String.format(MISSING_PROPERTY_EXCEPTION_FORMAT, property));
    }

    return defaultValue;
  }
}
