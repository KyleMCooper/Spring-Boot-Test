package com.accessofusion.skeleton.config;

import com.accessofusion.skeleton.RunIntegrationTestsCouchbase;
import com.accessofusion.multitenancy.config.model.TenantConfiguration;
import com.accessofusion.multitenancy.config.providers.PropertyFileTenantConfigurationProvider;
import com.accessofusion.multitenancy.config.providers.TenantConfigurationProvider;
import com.accessofusion.skeleton.junit.rules.ContainerRule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class overrides the connection string of all the tenants in order to work properly with the test container
 * dynamic ports. This class internally uses PropertyFileTenantConfigurationProvider to pull configurations from their
 * files.
 */
public class DynamicUrlTenantConfigurationProviderCouchbase implements TenantConfigurationProvider {
    private final String tenantFolderLocation;
    private List<TenantConfiguration> tenantConfigurationList;

    public DynamicUrlTenantConfigurationProviderCouchbase(@Value("${multitenancy.tenants.location}") String tenantFolderLocation) {
        this.tenantFolderLocation = tenantFolderLocation;
    }

    @PostConstruct
    public void initialize() {
        if (RunIntegrationTestsCouchbase.containerRule.isEnabled) {
            PropertyFileTenantConfigurationProvider provider = new PropertyFileTenantConfigurationProvider(tenantFolderLocation);
            String testContainerUrl = ContainerRule.couchbaseContainer.getConnectionString();

            tenantConfigurationList = provider.getTenants().stream().map(tc -> {
                tc.setUrl(testContainerUrl);
                return tc;
            }).collect(Collectors.toList());
        }
    }


    @Override
    public List<TenantConfiguration> getTenants() {
        return tenantConfigurationList;
    }
}
