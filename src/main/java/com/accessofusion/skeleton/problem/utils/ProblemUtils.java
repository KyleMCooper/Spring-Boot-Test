/*
 *
 *  (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.problem.utils;

import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

public class ProblemUtils {

  /**
   * Builds a ThrowableProblem with a default message and additional information when specified.
   */
  public static ThrowableProblem getProblem(Status status, String defaultMessage,
      String additionalInfo) {
    return Problem.builder().withStatus(status).withTitle(defaultMessage).withDetail(additionalInfo)
        .build();
  }
}
