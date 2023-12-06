/*
 * *
 *  * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.validator.custom;

import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import com.accessofusion.skeleton.validator.PatchRequestIsValidConstraint;
import com.accessofusion.skeleton.validator.PatchValidator;
import com.accessofusion.skeleton.validator.utils.ValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Validate patch requests for {@link SkeletonResource}. As request fields for the patch request are
 * optional, the validation are done only when te field is present.
 */
@Component
public class SkeletonResourcePatchValidator implements
    ConstraintValidator<PatchRequestIsValidConstraint, SkeletonResource>,
    PatchValidator<SkeletonResource> {

  @Override
  public boolean validate(SkeletonResource object, ConstraintValidatorContext context) {

    if (object == null) {
      ValidatorUtils.addViolation(context, ErrorMessages.PATCH_OBJECT_CAN_NOT_BE_NULL, ".");
      return false;
    }

    boolean isValid = true;

    //Validate the field only when it's present on the request.
    if (null != object.getName() && !StringUtils.hasText(object.getName())) {
      ValidatorUtils
          .addViolation(context, ErrorMessages.SKELETON_RESOURCE_NAME_MUST_NOT_BE_BLANK, "name");
      isValid = false;
    }

    return isValid;
  }

  @Override
  public boolean isValid(SkeletonResource value, ConstraintValidatorContext context) {
    return validate(value, context);
  }
}
