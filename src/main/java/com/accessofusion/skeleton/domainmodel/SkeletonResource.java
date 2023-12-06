package com.accessofusion.skeleton.domainmodel;

import com.accessofusion.skeleton.problem.ErrorMessages;
import com.accessofusion.skeleton.validator.PatchRequestIsValidConstraint;
import com.accessofusion.skeleton.validator.ValidatorGroups.CreateValidationGroup;
import com.accessofusion.skeleton.validator.ValidatorGroups.PatchValidationGroup;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@PatchRequestIsValidConstraint(message = ErrorMessages.PATCH_REQUEST_INVALID, groups = PatchValidationGroup.class)
public class SkeletonResource {

  /**
   * Id of the skeletonResource. TODO: check if this will be an integer
   */
  @ApiModelProperty(example = "1")
  @NotBlank(groups = CreateValidationGroup.class)
  private String id;
  @ApiModelProperty(example = "true", required = true)
  @NotNull(groups = CreateValidationGroup.class)
  private Boolean active;
  @ApiModelProperty(example = "string", required = true)
  @NotBlank(message = ErrorMessages.SKELETON_RESOURCE_NAME_MUST_NOT_BE_BLANK, groups = CreateValidationGroup.class)
  @Size(min = 1, max = 100, groups = {CreateValidationGroup.class, PatchValidationGroup.class})
  private String name;

}
