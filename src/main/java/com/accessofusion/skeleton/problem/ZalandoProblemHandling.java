package com.accessofusion.skeleton.problem;

import com.accessofusion.skeleton.config.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.violations.ConstraintViolationProblem;

/**
 * Zalando problems handling
 */
@ControllerAdvice
public class ZalandoProblemHandling implements ProblemHandling {

  public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity,
      NativeWebRequest request) {
    if (entity == null || entity.getBody() == null) {
      return entity;
    }
    Problem problem = entity.getBody();
    if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
      return entity;
    }
    ProblemBuilder builder = Problem.builder();
    builder.withStatus(problem.getStatus());
    builder.withTitle(problem.getTitle());
    builder.withDetail(problem.getDetail());

    if (problem instanceof ConstraintViolationProblem) {
      builder.with(Constants.VIOLATIONS_HOLDER,
          ((ConstraintViolationProblem) problem).getViolations());
    }

    return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
  }
}