/*
 *
 *  (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.service;

import static com.accessofusion.skeleton.problem.ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION;

import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@Service
public class SkeletonResourceServiceNone {

  private static final Logger LOGGER = LoggerFactory.getLogger(SkeletonResourceServiceNone.class);
  private final ModelMapper patchMapper;
  /**
   * TODO: remove this Just a map object to simulate crud actions. Used to keep sample values
   */
  Map<String, SkeletonResource> skeletonResourceMap = new HashMap<>();

  public SkeletonResourceServiceNone(@Qualifier("patchMapper") ModelMapper patchMapper) {
    this.patchMapper = patchMapper;
  }

  public SkeletonResource get(String skeletonResourceId) {
    validateSkeletonResourceExists(skeletonResourceId);
    return skeletonResourceMap.get(skeletonResourceId);
  }

  public Page<SkeletonResource> getAll(Pageable pageable) {
    List<SkeletonResource> skeletonResourceListResult;
    List<SkeletonResource> skeletonResourceList = new ArrayList<>(skeletonResourceMap.size());
    skeletonResourceList.addAll(skeletonResourceMap.values());

    skeletonResourceListResult = skeletonResourceList;
    if (null != pageable) {
      pageable.getSort().forEach(sort -> {
        //Sorting by name just for demo purposes
        if (sort.getDirection().isAscending()) {
          skeletonResourceList.sort(Comparator.comparing(SkeletonResource::getName));
        } else {
          skeletonResourceList.sort(Comparator.comparing(SkeletonResource::getName).reversed());
        }
      });

      if (pageable.getOffset() <= skeletonResourceList.size()) {
        if ((pageable.getOffset() + pageable.getPageSize()) < skeletonResourceList.size() - 1) {
          skeletonResourceListResult = skeletonResourceList
              .subList((int) pageable.getOffset(),
                  (int) (pageable.getOffset() + pageable.getPageSize()));
        } else {
          skeletonResourceListResult = skeletonResourceList
              .subList((int) pageable.getOffset(), skeletonResourceList.size());
        }
      }
    }

    return new PageImpl<>(skeletonResourceListResult, pageable, skeletonResourceMap.size());
  }

  public SkeletonResource create(SkeletonResource skeletonResource) {
    //TODO: add any extra business validation logic here
    validateSkeletonResourceForCreation(skeletonResource);
    skeletonResourceMap.put(skeletonResource.getId(), skeletonResource);
    return skeletonResourceMap.get(skeletonResource.getId());
  }

  public SkeletonResource update(SkeletonResource skeletonResource) {
    validateSkeletonResourceExists(skeletonResource.getId());
    //TODO: add any extra business validation logic here
    skeletonResourceMap.put(skeletonResource.getId(), skeletonResource);
    return skeletonResourceMap.get(skeletonResource.getId());
  }

  public SkeletonResource patch(SkeletonResource skeletonResource) {
    validateSkeletonResourceExists(skeletonResource.getId());
    //TODO: add any extra business validation logic here
    SkeletonResource skeletonResourceToPatch = get(skeletonResource.getId());
    patchMapper.map(skeletonResource, skeletonResourceToPatch);
    skeletonResourceMap.put(skeletonResource.getId(), skeletonResourceToPatch);
    return skeletonResourceMap.get(skeletonResource.getId());
  }

  /**
   * Validate if the skeletonResource with skeletonResourceId exists in the data source. If not
   * exists then a not found exception will be thrown
   */
  private void validateSkeletonResourceExists(String skeletonResourceId) {
    if (!skeletonResourceMap.containsKey(skeletonResourceId)) {
      throw Problem.valueOf(Status.NOT_FOUND,
          String.format(SKELETON_RESOURCE_NOTFOUND_EXCEPTION, skeletonResourceId));
    }
  }

  /**
   * Validate if the specified skeletonResource is valid for creation
   */
  private void validateSkeletonResourceForCreation(SkeletonResource skeletonResource) {
    Optional<Entry<String, SkeletonResource>> skeletonResourceFetched = skeletonResourceMap
        .entrySet().stream()
        .filter(entry -> skeletonResource.getName().equals(entry.getValue().getName()))
        .findFirst();
    if (skeletonResourceFetched.isPresent()) {
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
