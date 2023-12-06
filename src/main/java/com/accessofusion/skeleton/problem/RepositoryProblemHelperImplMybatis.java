/*
 *
 *  (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.problem;

import com.accessofusion.skeleton.problem.utils.ProblemUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@Component
public class RepositoryProblemHelperImplMybatis implements RepositoryProblemHelper {

  @Override
  public ThrowableProblem tryResolve(Exception exception, String additionalInfo) {
    if (exception instanceof DuplicateKeyException) {
      return ProblemUtils
          .getProblem(Status.CONFLICT, ErrorMessages.DUPLICATE_KEY_EXCEPTION,
              additionalInfo);
    }
    if (exception instanceof DataIntegrityViolationException) {
      return ProblemUtils
          .getProblem(Status.CONFLICT, ErrorMessages.DATA_INTEGRITY_EXCEPTION,
              additionalInfo);
    }
    if (exception instanceof DataAccessException) {
      return ProblemUtils
          .getProblem(Status.CONFLICT, ErrorMessages.DATA_ACCESS_EXCEPTION,
              additionalInfo);
    }
    return ProblemUtils
        .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_UNEXPECTED_ERROR,
            additionalInfo);
  }
}
