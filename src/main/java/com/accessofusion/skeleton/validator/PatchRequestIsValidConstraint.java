/*
 * *
 *  * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.validator;

import com.accessofusion.skeleton.validator.custom.SkeletonResourcePatchValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Constraint to be used to mark a domain model class to be validated on patch operations. Used in
 * conjunction with validations groups to provide conditional behavior.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SkeletonResourcePatchValidator.class)
public @interface PatchRequestIsValidConstraint {

  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
