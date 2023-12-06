/*
 *
 *  (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */
package com.accessofusion.skeleton.problem;

import com.accessofusion.skeleton.problem.utils.ProblemUtils;
import org.springframework.data.couchbase.core.CouchbaseQueryExecutionException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@Component
public class RepositoryProblemHelperImplCouchbase implements RepositoryProblemHelper {

  @Override
  public ThrowableProblem tryResolve(Exception exception, @Nullable String additionalInfo) {
    //Normally, there are not so many errors thrown by Couchbase. As a document database, a document doesn't have too many validations.
    if (exception instanceof CouchbaseQueryExecutionException) {
      //This problem might be thrown when the couchbase primary index for the bucket is absent and also for permission problems.
      return ProblemUtils
          .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_NOT_AVAILABLE_FOR_QUERIES,
              additionalInfo);
    }
    return ProblemUtils
        .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_UNEXPECTED_ERROR,
            additionalInfo);
  }
}
