package com.accessofusion.skeleton.repository;

import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SkeletonResourceRepository {

  SkeletonResource get(String skeletonResourceId);

  SkeletonResource create(SkeletonResource skeletonResource);

  Page<SkeletonResource> getAll(Pageable pageable);

  SkeletonResource update(SkeletonResource skeletonResource);

  SkeletonResource patch(SkeletonResource skeletonResource);

  /**
   * Verifies if there's any skeletonResource with the specified id
   */
  boolean existsById(String id);

  /**
   * Verifies if there's any skeletonResource with the specified name
   */
  boolean existsByName(String name);
}
