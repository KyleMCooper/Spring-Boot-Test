package com.accessofusion.skeleton.config;

import com.accesso.logutils.RequestResponseLogger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.accessofusion.multitenancy.config.providers.PropertyFileTenantConfigurationProvider;//Couchbase line
import com.accessofusion.multitenancy.config.providers.TenantConfigurationProvider;//Couchbase line
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;//Couchbase line
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;

/**
 * Exposes required configuration.
 */
@Configuration
public class SkeletonConfig {

  /**
   * When enabled in gradle (springBoot{ buildInfo() }), it will resolve the build information.
   */
  private Optional<BuildProperties> buildProperties;

  @Value("${multitenancy.tenants.location}")//Couchbase line
  String tenantFolderLocation;//Couchbase line

  @Autowired(required = false)
  public SkeletonConfig(Optional<BuildProperties> buildProperties) {
    this.buildProperties = buildProperties;
  }

  /**
   * The tags used in the API
   */
  @Bean("openApiTags")
  public List<Tag> getOpenApiTags() {
    //TODO: add custom tags
    return Arrays.asList(
        new Tag(Constants.SKELETON_RESOURCE_TAG_NAME, Constants.SKELETON_RESOURCE_OPERATIONS)
    );
  }

  @Bean("apiInfo")
  public ApiInfo getApiInfo() {
    //TODO: fill in the API information
    return new ApiInfoBuilder()
        .title("Set the title")
        .description("Add a short name")
        .license("private")
        .licenseUrl("https://accesso.com")
        .termsOfServiceUrl("")
        .version(buildProperties.isPresent() ? buildProperties.get().getVersion() : "1.0.0")
        .contact(new Contact("", "", ""))
        .build();
  }

  /**
   * Used to copy values between pojos
   */
  @Bean("modelMapper")
  public ModelMapper getModelMapper() {
    return new ModelMapper();
  }

  /**
   * Used as helper on patch operations
   */
  @Bean("patchMapper")
  public ModelMapper getPatchMapper() {
    ModelMapper patchMapper = getModelMapper();
    //It's the model mapper as configured in getModelMapper but customized to return only not null values.
    patchMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    return patchMapper;
  }

  /**
   * Global request/response logger.
   */
  @Bean
  public FilterRegistrationBean<RequestResponseLogger> requestResponseLogger() {
    FilterRegistrationBean<RequestResponseLogger> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new RequestResponseLogger());
    return registrationBean;
  }

  @Bean("tenantConfigurationProvider")//Couchbase line
  public TenantConfigurationProvider getTenantConfigurationProvider() {//Couchbase line
    return new PropertyFileTenantConfigurationProvider(tenantFolderLocation);//Couchbase line
  }//Couchbase line

}
