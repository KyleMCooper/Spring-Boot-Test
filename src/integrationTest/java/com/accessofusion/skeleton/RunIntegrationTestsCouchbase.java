package com.accessofusion.skeleton;

import com.accessofusion.skeleton.junit.rules.ContainerRule;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

/**
 * Class to run integrations tests Any @ClassRule annotated object should be declared here
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    glue = {"com.accessofusion.skeleton"},
    features = {"classpath:features/"},
    plugin = {"pretty", "html:build/cucumber/reports", "json:build/cucumber/cucumber.json"}
)
public class RunIntegrationTestsCouchbase {

  /**
   * Create an instance of couchbase container to share across the tests
   */
  @ClassRule
  public static ContainerRule containerRule = new ContainerRule();
}
