package com.accessofusion.skeleton.problem;

public class ErrorMessages {

  public static final String SKELETON_RESOURCE_NOTFOUND_EXCEPTION = "SkeletonResource with id %s not found";
  public static final String SKELETON_RESOURCE_NOT_INSERTED = "No records affected on insert";
  public static final String PATCH_REQUEST_INVALID = "Patch request invalid";
  public static final String PATCH_OBJECT_CAN_NOT_BE_NULL = "Object to patch can't be null";
  public static final String SKELETON_RESOURCE_NAME_MUST_NOT_BE_BLANK = "Name is empty or it only contains whitespaces";
  public static final String PATCH_VALIDATOR_REQUIRED = "Patch validator required";
  public static final String SQL_SORT_FIELD_VALUE_ERROR = "There is an error with sort field value %s";
  public static final String BACKEND_UNEXPECTED_ERROR = "Backend unexpected error";
  public static final String DUPLICATE_KEY_EXCEPTION = "Duplicate key exception";
  public static final String DATA_ACCESS_EXCEPTION = "Data source access exception";
  public static final String DATA_INTEGRITY_EXCEPTION = "Data source integrity contraint exception";
  public static final String BACKEND_NOT_AVAILABLE_FOR_QUERIES = "Backend not available for queries, please check the logs";//Couchbase line
  public static final String UPDATE_SKELETON_RESOURCE_EXCEPTION = "Exception while updating skeletonResource with id %s";
  public static final String FETCH_SKELETON_RESOURCES_EXCEPTION = "Exception while fetching skeletonResources";
  public static final String COUNT_SKELETON_RESOURCES_EXCEPTION = "Exception while counting skeletonResources";
  public static final String INSERT_SKELETON_RESOURCE_EXCEPTION = "Exception while inserting skeletonResource";
  public static final String INSERT_SKELETON_RESOURCE_DUPLICATE_EXCEPTION = "%s. Inserting skeletonResource with %s %s";
  public static final String FETCH_BY_ID_SKELETON_RESOURCE_EXCEPTION = "Exception while fetching skeletonResource with id %s";
  public static final String LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_ID_EXCEPTION = "Exception while looking if skeletonResource exists by id %s";
  public static final String LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_NAME_EXCEPTION = "Exception while looking if skeletonResource exists by name %s";
}
