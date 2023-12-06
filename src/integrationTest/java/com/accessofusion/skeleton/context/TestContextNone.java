/*
 *
 * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.context;

import static io.restassured.RestAssured.with;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.accessofusion.skeleton.SkeletonApplication;
import io.cucumber.java.BeforeStep;
import io.restassured.RestAssured;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

/**
 * Initialize the Spring boot test context. It launches a web server using a random port.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SkeletonApplication.class}, webEnvironment = RANDOM_PORT)
public class TestContextNone {

  private static Logger LOGGER = LoggerFactory.getLogger(TestContextNone.class);
  /**
   * Tells if the swagger file was already generated
   */
  private static boolean swaggerGenerated = false;

  /**
   * Holds the value for the Springboot test's randomly generated port
   */
  @Value("${local.server.port}")
  private int port;

  /**
   * Sets the Rest assure connection information and generate the swagger.json file for OpenAPI
   */
  @BeforeStep
  public void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
    RestAssured.basePath = "";
    if (!swaggerGenerated) {
      generateApiDocs();
      swaggerGenerated = true;
    }
  }

  /**
   * Trick to generate swagger.json definition file when using mybatis as datasource.
   */
  private void generateApiDocs() {
    String outputDir = System.getProperty("staticjson.outputDir");
    if (!StringUtils.hasText(outputDir)) {
      outputDir = "swagger/";
    }

    try {
      String swaggerJson = with().get("/apidocs").then().extract().asString();
      Files.createDirectories(Paths.get(outputDir));
      try (BufferedWriter writer = Files
          .newBufferedWriter(Paths.get(outputDir, "swagger.json"), StandardCharsets.UTF_8)) {
        writer.write(swaggerJson);
      } catch (Exception s) {
        LOGGER.error("An exception was thrown while trying to update the swagger file {}",
            s.getMessage(), s);
      }
    } catch (Exception s) {
      LOGGER.error("An exception was thrown while trying to create the swagger folder{}",
          s.getMessage(), s);
    }
  }


}
