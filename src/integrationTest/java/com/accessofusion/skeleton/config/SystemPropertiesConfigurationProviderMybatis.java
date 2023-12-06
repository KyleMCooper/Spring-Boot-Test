/*
 *
 * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */
package com.accessofusion.skeleton.config;

import com.accessofusion.multitenancy.config.model.TenantConfiguration;
import com.accessofusion.multitenancy.config.providers.TenantConfigurationProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Read configuration from system properties instead of tenant files. It can be useful for testing
 * relational databases with test containers as system properties can be set after container
 * initialization.
 */
public class SystemPropertiesConfigurationProviderMybatis implements TenantConfigurationProvider {

  @Override
  public List<TenantConfiguration> getTenants() {
    List<TenantConfiguration> tenantConfigurations = new ArrayList<>();
    String tenantsString = System.getProperty("tenants");
    String[] tenants = tenantsString.split(",");
    for (int i = 0; i < tenants.length; i++) {
      TenantConfiguration tenantConfiguration = new TenantConfiguration();
      tenantConfiguration.setSource("system");
      tenantConfiguration.setName(System.getProperty(tenants[i] + ".name"));
      tenantConfiguration.setDriver(System.getProperty(tenants[i] + ".driver"));
      tenantConfiguration.setUrl(System.getProperty(tenants[i] + ".url"));
      tenantConfiguration.setUsername(System.getProperty(tenants[i] + ".username"));
      tenantConfiguration.setPassword(System.getProperty(tenants[i] + ".password"));
      tenantConfigurations.add(tenantConfiguration);
    }
    return tenantConfigurations;
  }
}
