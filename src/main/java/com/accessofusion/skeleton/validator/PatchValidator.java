/*
 *
 *   (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.validator;

import jakarta.validation.ConstraintValidatorContext;

/**
 * Common validation interface for patch requests. As it's common to check for required fields in
 * the patch requests, this interface has a default method for that. It can be overridden as
 * needed.
 */
public interface PatchValidator<T> {

  /**
   * Custom validation for patch method
   */
  boolean validate(T object, ConstraintValidatorContext context);
}
