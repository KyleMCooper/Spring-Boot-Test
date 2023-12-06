/*
 *
 *  (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
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
public class RunIntegrationTestsNone {

}
