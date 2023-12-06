# SpringMicroservicesTemplate

## Getting started

This repository is intended to be used as a template for Spring Boot REST microservices, with optional couchbase or mybatis data source.

Please visit this link for more information: https://accessoclientservices.atlassian.net/wiki/spaces/P2/pages/683049235/Using+the+Spring+Microservices+Template.

### What's included:
This template project includes:

- Working multitenancy (couchbase / mybatis). The Mybatis library supports most popular JDBC drivers. H2 driver is added by default for Mybatis projects.
- Basic CRUD endpoints intended to show the use of open api annotations and jakarta validations.
- Basic domain model object with three fields ( id: String, name: String, active: Boolean). For testing purposes, id is the primary key and name is a unique field.
- Zalando problem handling. Including a helper class (com.accessofusion.skeleton.problem.RepositoryProblemHelperImpl) to translate the most common exceptions for data source interactions.
- Basic unit tests to demonstrate how to validate Zalando Problems.
- Basic integration tests to show the usage of cucumber alongside rest-assured to test API endpoints.    
- Swagger 2 / oas3 file generation from annotations. A gradle task is provided.
- Global request/response logging using the LogUtils library
- Dependencies imported from custom Accesso BOM files. 
- Google style format as a Gradle task. 

### Project structure:
- src/main/src: application sources 
- src/main/test: basic junit with no database or external service integration. 
- src/main/integrationTest: BBD style testing running the application against an "integration database" or virtualized services. TestContainers is used to run a custom couchbase instance or a mysql database for Mybatis projects.

### Main packages:

src/main/

├── java

│   └── com

│       └── accessofusion

│           └── skeleton : service package (it should be replaced for a meaningful name.) 

│               ├── api : Interfaces to be exposed as rest controllers.

│               ├── config : Specific project configuration classes

│               ├── controller : Implementation fo the interfaces in the api package.

│               ├── dao : Mybatis mappers or couchbase repositories. 

│               │   └── sqlproviders : only required for mybatis (paging and sorting support).

│               ├── datamodel : service data model

│               ├── domainmodel : service domain model

│               ├── logging : Custom logging logic (extending LogUtils logger)

│               ├── problem : Zalando configuration and error messages.

│               ├── repository : Repository declaration and implementation.

│               ├── service : "Service" classes 

│               └── validator : Custom jakarta validations

└── resources

    ├── db
    
    └── tenants
    
### Before start:
There's some setup required before first "run". It's very important to know your data source target before start. There are two option for target data sources:
- mybatis: used for JDBC compatible data sources.
- couchbase: used for Couchbase.
- none:  for no database.

Also, there are code lines ending with comments like "//Couchbase line". Those are special markers used by the task to perform replacements. Please, don't do any formatting if you want to use the gradle task provided for customization.

The gradle command used in this README is `gradlew ...`. Take into account that you want to use this command on *nix terminals(including git bash or wsl bash), you will need to type `./gradlew ..` instead.

In order to do the first project setup, you can go manually or use a custom gradle task named _quickStart_ to speed the process. For both scenarios, there are specific TODOs place where manual customization is required.    

### Manual setup:
- Remove dependencies in build.gradle not related to the target data source. Please, left only the lines marked for the target data source to avoid conflicts. 
- Classes for specific data sources are suffixed with the data source name, you can remove those classes to avoid compilation problems.
- Refactor as required. The word "Skeleton" is used to mark places where customization should be done. As a suggestion, start by refactoring the domainmodel and datamodel classes. 

### Using custom _quickStart_ gradle task:

This a gradle task provided to simplify the setup process. The project include some classes with the name Skeleton and SkeletonResource, with this task string substitutions will be performed to have a initial working project. 

#### Requirements:
It's not mandatory, but if you want to use the gradle executable installed in your environment instead of provided gradlew, please ensure that gradle is available in the system path and gradle's version is greater than 5.4.1.

#### Using the _quickStart_ Task:
 
Example:
```
gradlew quickStart clean build -Ptarget=couchbase -Pquickstart=true -Pdefault_resource=Product -Papp_name=ProductAdmin --refresh-dependencies
```
##### Parameters:
- quickstart: true|false (Mandatory) 
  If set to true the task will perform transformations
- target: mybatis|couchbase|none (Mandatory)
  Set the target datasource for this microservice
- default_resource: set the name of the default resource exposed in this API (for example: Product, Car, User, Customer, UserRole). At this moment there's no validation for special characters or spaces.  
- app_name: set the API name (for example: ProductAdmin, ClientAdmin, UserAdmin). It will be used to set the package names. Please don't use special characters or whitespaces.

It performs the following actions(in the listed order):
 - Remove classes not intended for the target data source  
 - Rename all the classes named SkeletonResource* to the value passed in default_resource
 - Rename all the classes named Skeleton* to the value passed in app_name
 - Rename all the packages named skeleton to the lower case value of app_name
 - In every step, text replacements will be performed at code level to create a version ready to run
 
#### Additional customization
There's another task provided to help developers to remove unwanted endpoints. To run this customization you can use the following command:
```
gradlew customizeFirstVersion -Pquickstart=true -Ptarget=couchbase -Pdefault_resource=Product -Papp_name=ProductAdmin -Pwith_get=true -Pwith_get_all=true -Pwith_post=true -Pwith_put=true -Pwith_patch=true --offline --stacktrace
``` 
##### Parameters:
Please pass the same parameters and values used to run the _quickStart_ task.

- with_get: true|false
Used to include(if value equals true) or exclude the endpoint to get a resource by id.
 
- with_get_all: true|false
Used to include(if value equals true) or exclude the endpoint to get all resources with pagination(HATEOAS style).
 
- with_post=true|false
Used to include(if value equals true) or exclude the endpoint to create a resource.
 
- with_put=true|false
Used to include(if value equals true) or exclude the endpoint to update a resource by id.

- with_patch=true|false
Used to include(if value equals true) or exclude the endpoint to partially update a resource by id.

##### Example for Mybatis. Using Product as default resource and ProductAdmin as API name:  
`gradlew quickStart clean build -Ptarget=mybatis -Pquickstart=true -Pdefault_resource=Product -Papp_name=ProductAdmin --refresh-dependencies`

##### Example for Couchbase. Using Product as default resource and ProductAdmin as API name:  
`gradlew quickStart clean build -Ptarget=couchbase -Pquickstart=true -Pdefault_resource=Product -Papp_name=ProductAdmin --refresh-dependencies` 

##### Example for no data source. Using Product as default resource and ProductAdmin as API name:  
`gradlew quickStart clean build -Ptarget=none -Pquickstart=true -Pdefault_resource=Product -Papp_name=ProductAdmin --refresh-dependencies` 

#### Running the app:
You can normally run the app using Intellij or by using `gradlew bootRun`. Please consider the following:
##### For couchbase:
- Connection data should be added on application.properties. A default tenant configuration is always required.
- A primary index is required for every target bucket. Example:`CREATE PRIMARY INDEX ``#primary`` ON tenant1;`
##### For mybatis:
- H2 is provided to run in memory tests. If you want to use another RDBMS please update the connection files in the tenants folder.
- It's required one file for each tenant in the tenants folder. Also, it's important to notice that it's mandatory to have one default tenant file.

#### Start over:
If you missed a step or just want to start over again, please use the git revert feature or checkout the repository again.

## Misc:

### Exclude multitenancy libraries.
If the project that you need to setup does not require multitenancy, you can proceed as following: 
- Setup the project as mentioned in the sections before.
- Add the multitenancy exclusion for the target datasource. Examples:
```
  implementation('com.accessofusion:spring-microservices-mybatis-bom:2.0.0-SNAPSHOT'){
    exclude group: 'com.accessofusion', module: 'mybatis-multitenancy-utils'
  }
```  
```
  implementation('com.accessofusion:spring-microservices-couchbase-bom:2.0.0-SNAPSHOT'){
    exclude group: 'com.accessofusion', module: 'couchbase-multitenancy-utils'
  }
```

### Running the integration tests:
In both scenarios, after the gradle tasks finishes, the project should be stable. The provided integration tests should run without errors:

`gradlew integrationTests --tests com.accessofusion.{appName}.RunIntegrationTests`

{appName} should be replaced with the value passed for -Papp_name in lowercase.

Note: To run the couchbase tests with TestContainers you must set *use_container_for_tests* env variable to *true*. Otherwise, you will need to add the tenant configuration in application.properties for the integrationTest source set.

### Generate Open Api Specification v3 file (it depends on running the integration tests):
Open API specification v3 and v2(aka swagger2) files are generated on each integration tests run. As a recommendation, use a continuous integration tool to copy those files to a documentation server.


