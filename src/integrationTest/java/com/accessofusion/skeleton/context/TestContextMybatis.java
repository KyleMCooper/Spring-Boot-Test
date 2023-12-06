package com.accessofusion.skeleton.context;

import static io.restassured.RestAssured.with;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.accessofusion.skeleton.SkeletonApplication;
import com.accessofusion.skeleton.config.SystemPropertiesConfigurationProviderMybatis;
import com.accessofusion.skeleton.utils.IntegrationTestsUtils;
import io.cucumber.java.BeforeStep;
import io.restassured.RestAssured;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.MySQLContainer;

/**
 * Initialize the Spring boot test context. It launches a web server using a random port.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SkeletonApplication.class}, webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = TestContextMybatis.Initializer.class)
public class TestContextMybatis {

  /**
   * MySQL docker container initialization
   */
  @ClassRule
  public static MySQLContainer mySQLContainer = new MySQLContainer(
      IntegrationTestsUtils.getContainerImageTag())
      .withUsername("test").withPassword("test").withDatabaseName("integration_tests");
  private static Logger LOGGER = LoggerFactory.getLogger(TestContextMybatis.class);
  /**
   * Tells if the swagger file was already generated
   */
  private static boolean swaggerGenerated = false;

  static {
    //Copy files required for setup
    mySQLContainer.withClasspathResourceMapping("db/integration-tests-schema.sql",
        "/opt/integration-tests-schema.sql", BindMode.READ_ONLY);
    mySQLContainer.withClasspathResourceMapping("db/dbsetup.sh",
        "/opt/dbsetup.sh", BindMode.READ_ONLY);
    mySQLContainer.start();
    try {
      //Execute the command to setup the database
      ExecResult execResult = mySQLContainer.execInContainer("bash", "/opt/dbsetup.sh");
      LOGGER.info("Database setup result {}", execResult.getExitCode());
      LOGGER.info(execResult.getStdout());
      LOGGER.error(execResult.getStderr());
      //Abort execution if there's any problem with the setup
      assertEquals(0, execResult.getExitCode());
    } catch (IOException e) {
      LOGGER.error("IOException during integration tests database setup", e);
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      LOGGER.error("InterruptedException during integration tests database setup", e);
      throw new RuntimeException(e);
    }
  }

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

  /**
   * Initializer class to add datasource properties before application is ready
   */
  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     *
     */
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      // Set the default tenant properties after container initialization
      System.setProperty("tenants", "default");
      System.setProperty("default.name", "default");
      System.setProperty("default.driver", mySQLContainer.getDriverClassName());
      System.setProperty("default.url", mySQLContainer.getJdbcUrl() + "?useSSL=false");
      System.setProperty("default.username", mySQLContainer.getUsername());
      System.setProperty("default.password", mySQLContainer.getPassword());
      configurableApplicationContext
          .getBeanFactory()
          .registerSingleton(
              "tenantConfigurationProvider", new SystemPropertiesConfigurationProviderMybatis());
    }
  }
}
