/*
 * *
 *  * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.validator.utils;

import java.util.Objects;
import jakarta.validation.ConstraintValidatorContext;

public class ValidatorUtils {

  /**
   * Adds a new violation to the constraint validator context
   */
  public static void addViolation(ConstraintValidatorContext context, String messageTemplate,
      String field) {
    Objects.requireNonNull(context);
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(messageTemplate).addPropertyNode(field)
        .addConstraintViolation();
  }
}
