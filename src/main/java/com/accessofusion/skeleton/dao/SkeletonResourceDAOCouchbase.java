package com.accessofusion.skeleton.dao;

import com.accessofusion.multitenancy.repository.N1qlCouchbaseRepository;
import com.accessofusion.skeleton.datamodel.SkeletonResourceEntity;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Couchbase N1ql repository for SkeletonResource. It provides basic CRUD operations including a
 * "fetch all" method with paging and sorting.
 */
@Repository
public interface SkeletonResourceDAOCouchbase extends
    N1qlCouchbaseRepository<SkeletonResourceEntity, String> {

  /**
   * If the skeletonResource with the specified name exists, It will return an skeletonResource
   * object only with its id.
   */
  @Query("SELECT META().id AS _ID, META().cas AS _CAS FROM #{#n1ql.bucket} WHERE #{#n1ql.filter} and name = $1")
  SkeletonResourceEntity findIdByName(String name);
}