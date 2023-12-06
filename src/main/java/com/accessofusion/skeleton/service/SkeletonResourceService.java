package com.accessofusion.skeleton.service;

import static com.accessofusion.skeleton.problem.ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION;

import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import com.accessofusion.skeleton.repository.SkeletonResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@Service
public class SkeletonResourceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SkeletonResourceService.class);

  private final SkeletonResourceRepository skeletonResourceRepository;

  public SkeletonResourceService(
      SkeletonResourceRepository skeletonResourceRepository) {
    this.skeletonResourceRepository = skeletonResourceRepository;
  }

  public SkeletonResource get(String skeletonResourceId) {
    validateSkeletonResourceExists(skeletonResourceId);
    return skeletonResourceRepository.get(skeletonResourceId);
  }

  public Page<SkeletonResource> getAll(Pageable pageable) {
    return skeletonResourceRepository.getAll(pageable);
  }

  public SkeletonResource create(SkeletonResource skeletonResource) {
    //TODO: add any extra business validation logic here
    validateSkeletonResourceForCreation(skeletonResource);
    return skeletonResourceRepository.create(skeletonResource);
  }

  public SkeletonResource update(SkeletonResource skeletonResource) {
    validateSkeletonResourceExists(skeletonResource.getId());
    //TODO: add any extra business validation logic here
    return skeletonResourceRepository.update(skeletonResource);
  }

  public SkeletonResource patch(SkeletonResource skeletonResource) {
    validateSkeletonResourceExists(skeletonResource.getId());
    //TODO: add any extra business validation logic here
    return skeletonResourceRepository.patch(skeletonResource);
  }

  /**
   * Validate if the skeletonResource with skeletonResourceId exists in the data source. If not
   * exists then a not found exception will be thrown
   */
  private void validateSkeletonResourceExists(String skeletonResourceId) {
    if (!skeletonResourceRepository.existsById(skeletonResourceId)) {
      throw Problem.valueOf(Status.NOT_FOUND,
          String.format(SKELETON_RESOURCE_NOTFOUND_EXCEPTION, skeletonResourceId));
    }
  }

  /**
   * Validate if the specified skeletonResource is valid for creation
   */
  private void validateSkeletonResourceForCreation(SkeletonResource skeletonResource) {
    if (skeletonResourceRepository.existsByName(skeletonResource.getName())) {
      throw logAndGetThrowable(Status.CONFLICT, String
          .format(ErrorMessages.INSERT_SKELETON_RESOURCE_DUPLICATE_EXCEPTION,
              ErrorMessages.DUPLICATE_KEY_EXCEPTION, "name",
              skeletonResource.getName()));
    }
  }

  /**
   * Logs an error message and returns a throwable problem with the message specified.
   *
   * @param status Zalando status.
   * @param message exception message to log.
   * @return @return A throwable problem based on the parameters provided.
   */
  private ThrowableProblem logAndGetThrowable(Status status, String message) {
    LOGGER.error(message);
    return Problem.valueOf(status, message);
  }
}
