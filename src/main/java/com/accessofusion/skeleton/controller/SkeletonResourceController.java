package com.accessofusion.skeleton.controller;

import com.accessofusion.skeleton.api.SkeletonResourceApi;
import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.service.SkeletonResourceService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SkeletonResourceController implements SkeletonResourceApi {

  private final SkeletonResourceService skeletonResourceService;

  public SkeletonResourceController(SkeletonResourceService skeletonResourceService) {
    this.skeletonResourceService = skeletonResourceService;
  }

  @Override
  public SkeletonResource get(String skeletonResourceId) {
    return skeletonResourceService.get(skeletonResourceId);
  }

  @Override
  public PagedModel<EntityModel<SkeletonResource>> getAllPaged(
      PagedResourcesAssembler<SkeletonResource> assembler, Pageable pageable) {
    return assembler.toModel(skeletonResourceService.getAll(pageable));
  }

  @Override
  public SkeletonResource create(SkeletonResource skeletonResource) {
    return skeletonResourceService.create(skeletonResource);
  }

  @Override
  public SkeletonResource update(String skeletonResourceId, SkeletonResource skeletonResource) {
    //The id used is the passed as skeletonResourceId parameter
    skeletonResource.setId(skeletonResourceId);
    return skeletonResourceService.update(skeletonResource);
  }

  @Override
  public SkeletonResource patch(String skeletonResourceId,
      SkeletonResource skeletonResource) {
    //The id used is the passed as skeletonResourceId parameter
    skeletonResource.setId(skeletonResourceId);
    return skeletonResourceService.patch(skeletonResource);
  }
}
