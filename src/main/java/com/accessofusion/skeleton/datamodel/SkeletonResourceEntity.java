package com.accessofusion.skeleton.datamodel;

import org.springframework.data.annotation.Id;//Couchbase line
import lombok.Data;

@Data
public class SkeletonResourceEntity {
  /**
   * Id of the skeletonResource. TODO: check if this will be an integer
   */
  @Id//Couchbase line
  private String id;
  private boolean active;
  private String name;
}
