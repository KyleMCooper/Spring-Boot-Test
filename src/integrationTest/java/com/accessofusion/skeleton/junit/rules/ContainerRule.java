package com.accessofusion.skeleton.junit.rules;

import com.accessofusion.skeleton.utils.IntegrationTestsUtils;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.user.Role;
import com.couchbase.client.java.manager.user.User;
import java.util.Collections;
import java.util.List;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Junit rule to initialize TestContainer's Couchbase module before the integrations tests start.
 */
public final class ContainerRule extends ExternalResource {

  private static final String DEFAULT_ADMIN = "admin";
  private static final String DEFAULT_PASSWORD = "password";
  private static final String TENANT_1 = "tenant1";
  private static final String TENANT_2 = "tenant2";
  private static final String USE_CONTAINER_FOR_INTEGRATION_TEST = "use_container_for_tests";
  /**
   * TestContainer instance for couchbase
   */
  public static CouchbaseContainer couchbaseContainer;
  private static Logger LOGGER = LoggerFactory.getLogger(ContainerRule.class);
  /**
   * Permissions for tenant1
   */
  private static List<Role> roles1 = Collections
      .singletonList(new Role("bucket_full_access", TENANT_1));
  /**
   * Permissions for tenant2
   */
  private static List<Role> roles2 = Collections
      .singletonList(new Role("bucket_full_access", TENANT_2));
  /**
   * tenant1 user configuration.
   */
  private static User userSettings = new User(TENANT_1).password(DEFAULT_PASSWORD).roles(roles1);
  /**
   * tenant2 user configuration.
   */
  private static User userSettingsForBucket2 = new User(TENANT_2).password(DEFAULT_PASSWORD).roles(roles2);
  /**
   * Check if container tests are enabled
   */
  public boolean isEnabled = false;

  public ContainerRule() {
    super();
    initCouchbaseContainer();
  }

  private void initCouchbaseContainer() {
    if ("true".equalsIgnoreCase(IntegrationTestsUtils
        .resolveSystemOrEnvironmentProperty(USE_CONTAINER_FOR_INTEGRATION_TEST, "false", false))) {
      isEnabled = true;
      setupContainer();
    }
  }

  /**
   * Setup two buckets, tenant1 and tenant2
   */
  private void setupContainer() {

    BucketDefinition tenant1Bucket = new BucketDefinition(TENANT_1);
    tenant1Bucket.withFlushEnabled(true).withQuota(100)
            .withPrimaryIndex(true);

    BucketDefinition tenant2Bucket = new BucketDefinition(TENANT_2);
    tenant1Bucket.withFlushEnabled(true).withQuota(100)
            .withPrimaryIndex(true);

    couchbaseContainer = new CouchbaseContainer(
            DockerImageName
                    .parse(IntegrationTestsUtils.getContainerImageTag())
                    .asCompatibleSubstituteFor("couchbase/server")
    );
    couchbaseContainer
            .withBucket(tenant1Bucket)
            .withBucket(tenant2Bucket)
            .withCredentials(DEFAULT_ADMIN, DEFAULT_PASSWORD)
            .withSharedMemorySize(256L);
    couchbaseContainer.start();
  }

  /**
   * Initializes the users required for test. Intended to be called on context initialization.
   */
  public void initializeContext(ConfigurableApplicationContext configurableApplicationContext) {
    if (isEnabled) {
      Cluster cluster = null;
      try {
        User user3 = new User(TENANT_1).password(DEFAULT_PASSWORD).roles(roles1);
        User user4 = new User(TENANT_2).password(DEFAULT_PASSWORD).roles(roles2);
        cluster = Cluster.connect(couchbaseContainer.getConnectionString(), couchbaseContainer.getUsername(),
                couchbaseContainer.getPassword());
        cluster.users().upsertUser(user3);
        cluster.users().upsertUser(user4);

        CreatePrimaryQueryIndexOptions opts = CreatePrimaryQueryIndexOptions
                .createPrimaryQueryIndexOptions()
                .ignoreIfExists(true);

        cluster.queryIndexes().createPrimaryIndex(TENANT_1, opts);
        cluster.queryIndexes().createPrimaryIndex(TENANT_2, opts);

      } catch (Exception s) {
        LOGGER.error("Exception trying to setup the container", s);
      } finally {
        if (cluster != null) {
          cluster.close();
        }
      }
      //+ couchbaseContainer.getMappedPort(8091) is not working when using community version. Used fixed port instead.
      //Filling properties for integration test. It's required a default connection for spring and at least one tenant with tenant1 as tenantId
      TestPropertyValues values = TestPropertyValues.of(
          //Default tenant is pointing to tenant1 just for testing purposes (Having a specific bucket will require more RAM).
          "tenant.dataSources[0].tenantId=default",
          "tenant.dataSources[0].url=couchbase://" + couchbaseContainer.getContainerIpAddress()
              + ":8091",
          "tenant.dataSources[0].bucket=" + TENANT_1,
          "tenant.dataSources[0].username=" + DEFAULT_ADMIN,
          "tenant.dataSources[0].password=" + DEFAULT_PASSWORD,
          "tenant.dataSources[1].tenantId=" + TENANT_1,
          "tenant.dataSources[1].url=couchbase://" + couchbaseContainer.getContainerIpAddress()
              + ":8091",
          "tenant.dataSources[1].bucket=" + TENANT_1,
          "tenant.dataSources[1].username=" + TENANT_1,
          "tenant.dataSources[1].password=" + DEFAULT_PASSWORD,
          "tenant.dataSources[2].tenantId=" + TENANT_2,
          "tenant.dataSources[2].url=couchbase://" + couchbaseContainer.getContainerIpAddress()
              + ":8091",
          "tenant.dataSources[2].bucket=" + TENANT_2,
          "tenant.dataSources[2].username=" + TENANT_2,
          "tenant.dataSources[2].password=" + DEFAULT_PASSWORD
      );
      values.applyTo(configurableApplicationContext);
    }
  }

}