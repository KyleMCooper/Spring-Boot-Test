/*
 *
 *  (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.problem;

import com.accessofusion.skeleton.problem.utils.ProblemUtils;
import org.springframework.lang.Nullable;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

/**
 * Helper class to translate data source thrown exceptions to more friendly Zalando problems
 */
public interface RepositoryProblemHelper {

  /**
   * Try to resolve the exception to its known subtypes.
   */
  default ThrowableProblem tryResolve(Exception exception, @Nullable String additionalInfo) {

    return ProblemUtils
        .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_UNEXPECTED_ERROR,
            additionalInfo);
  }
}
