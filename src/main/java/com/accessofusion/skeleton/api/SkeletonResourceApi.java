package com.accessofusion.skeleton.api;

import com.accessofusion.skeleton.config.Constants;
import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.validator.ValidatorGroups.CreateValidationGroup;
import com.accessofusion.skeleton.validator.ValidatorGroups.PatchValidationGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

/**
 * The interface SkeletonResourceApi is used to declare the exposed API operations.
 */
@Api(tags = {Constants.SKELETON_RESOURCE_TAG_NAME})
@RequestMapping("skeletonResource")
@Validated
public interface SkeletonResourceApi {

  /**
   * Finds a SkeletonResource by its id
   *
   * @param skeletonResourceId id
   * @return the skeletonResource if found, 404 error if don't.
   */
  @ApiOperation(value = "Returns a single skeletonResource.", nickname = "get")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = Constants.SUCCESSFUL_OPERATION, response = SkeletonResource.class),
      @ApiResponse(code = 403, message = Constants.FORBIDDEN, response = Problem.class),
      @ApiResponse(code = 404, message = Constants.SKELETON_RESOURCE_NOT_FOUND, response = Problem.class),
      @ApiResponse(code = 500, message = Constants.UNEXPECTED_ERROR, response = Problem.class)})
  @GetMapping(value = "/{skeletonResourceId}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      headers = {Constants.TENANT_HEADER, Constants.VERSION1_HEADER})
  SkeletonResource get(
      @ApiParam(value = "SkeletonResource id.", required = true) @PathVariable("skeletonResourceId") @NotNull String skeletonResourceId);

  /**
   * Gets all the records with HATEOAS pagination
   */
  @ApiOperation(value = "Returns a paged list of skeletonResources.", nickname = "getAllPaged")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = Constants.SUCCESSFUL_OPERATION),
      @ApiResponse(code = 403, message = Constants.FORBIDDEN, response = Problem.class),
      @ApiResponse(code = 500, message = Constants.UNEXPECTED_ERROR, response = Problem.class)})
  @GetMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      headers = {Constants.TENANT_HEADER, Constants.VERSION1_HEADER})
  PagedModel<EntityModel<SkeletonResource>> getAllPaged(
      PagedResourcesAssembler<SkeletonResource> assembler, Pageable pageable);

  /**
   * Creates one skeletonResource
   *
   * @param skeletonResource object to be created
   * @return the created skeletonResource with its id. If the format of skeletonResource is not
   * valid a bad request exception will be thrown.
   */
  @ApiOperation(value = "Creates a single skeletonResource.", nickname = "create")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = Constants.SUCCESSFUL_OPERATION, response = SkeletonResource.class),
      @ApiResponse(code = 400, message = Constants.BAD_REQUEST, response = ConstraintViolationProblem.class),
      @ApiResponse(code = 403, message = Constants.FORBIDDEN, response = Problem.class),
      @ApiResponse(code = 500, message = Constants.UNEXPECTED_ERROR, response = Problem.class)
  })
  @PostMapping(
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE},
      headers = {Constants.TENANT_HEADER, Constants.VERSION1_HEADER})
  @ResponseStatus(HttpStatus.CREATED)
  SkeletonResource create(
      @ApiParam(value = "SkeletonResource to create.", required = true) @Validated(CreateValidationGroup.class) @RequestBody SkeletonResource skeletonResource);

  /**
   * Updates one skeletonResource
   *
   * @param skeletonResourceId id of the object to be updated
   * @param skeletonResource object to be updated (all the fields are required for update)
   * @return the updated object
   */
  @ApiOperation(value = "Updates a single skeletonResource.", nickname = "update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = Constants.SUCCESSFUL_OPERATION, response = SkeletonResource.class),
      @ApiResponse(code = 400, message = Constants.BAD_REQUEST, response = ConstraintViolationProblem.class),
      @ApiResponse(code = 403, message = Constants.FORBIDDEN, response = Problem.class),
      @ApiResponse(code = 404, message = Constants.SKELETON_RESOURCE_NOT_FOUND, response = Problem.class),
      @ApiResponse(code = 500, message = Constants.UNEXPECTED_ERROR, response = Problem.class)
  })
  @PutMapping(value = "/{skeletonResourceId}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE},
      headers = {Constants.TENANT_HEADER, Constants.VERSION1_HEADER})
  SkeletonResource update(
      @ApiParam(value = "SkeletonResource id.", required = true) @PathVariable("skeletonResourceId") String skeletonResourceId,
      @ApiParam(value = "SkeletonResource to update.", required = true) @Validated(CreateValidationGroup.class) @RequestBody SkeletonResource skeletonResource);

  /**
   * Patches one skeletonResource
   *
   * @param skeletonResourceId id of the object to be patched
   * @param skeletonResource object to be patched (all the fields are optional for patch)
   * @return the patched object
   */
  @ApiOperation(value = "Patches a single skeletonResource.", nickname = "patch")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = Constants.SUCCESSFUL_OPERATION, response = SkeletonResource.class),
      @ApiResponse(code = 400, message = Constants.BAD_REQUEST, response = ConstraintViolationProblem.class),
      @ApiResponse(code = 403, message = Constants.FORBIDDEN, response = Problem.class),
      @ApiResponse(code = 404, message = Constants.SKELETON_RESOURCE_NOT_FOUND, response = Problem.class),
      @ApiResponse(code = 500, message = Constants.UNEXPECTED_ERROR, response = Problem.class)})
  @PatchMapping(value = "/{skeletonResourceId}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE},
      headers = {Constants.TENANT_HEADER, Constants.VERSION1_HEADER})
  SkeletonResource patch(
      @ApiParam(value = "SkeletonResource id.", required = true) @PathVariable("skeletonResourceId") String skeletonResourceId,
      @ApiParam(value = "SkeletonResource to patch.", required = true) @Validated(PatchValidationGroup.class) @RequestBody SkeletonResource skeletonResource);

}
