package com.accessofusion.skeleton.repository;

import com.accessofusion.multitenancy.config.couchbase.RepositoryService;
import com.accessofusion.skeleton.dao.SkeletonResourceDAOCouchbase;
import com.accessofusion.skeleton.datamodel.SkeletonResourceEntity;
import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import com.accessofusion.skeleton.problem.RepositoryProblemHelper;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.zalando.problem.ThrowableProblem;

@Repository
public class SkeletonResourceRepositoryImplCouchbase implements SkeletonResourceRepository {

  private final static Logger LOGGER = LoggerFactory
      .getLogger(SkeletonResourceRepositoryImplCouchbase.class);
  /**
   * Multitenancy repository service. It will return a different repository for each tenant
   * configured.
   */
  private final RepositoryService repositoryService;

  private final ModelMapper modelMapper;
  /**
   * Model mapper instance configured to only map non null values.
   */
  private final ModelMapper patchMapper;

  /**
   * Translates common data source interaction exceptions to Zalando problems.
   */
  private final RepositoryProblemHelper repositoryProblemHelper;

  public SkeletonResourceRepositoryImplCouchbase(
      RepositoryService repositoryService, @Qualifier("modelMapper") ModelMapper modelMapper,
      @Qualifier("patchMapper") ModelMapper patchMapper,
      RepositoryProblemHelper repositoryProblemHelper) {
    this.repositoryService = repositoryService;
    this.modelMapper = modelMapper;
    this.patchMapper = patchMapper;
    this.repositoryProblemHelper = repositoryProblemHelper;
  }

  private SkeletonResourceDAOCouchbase getDAO() {
    return repositoryService.getRepository(SkeletonResourceDAOCouchbase.class);
  }

  @Override
  public SkeletonResource get(String skeletonResourceId) {
    return getSkeletonResourceEntity(skeletonResourceId).map(this::asSkeletonResource).get();
  }

  @Override
  public SkeletonResource create(SkeletonResource skeletonResource) {
    try {
      return asSkeletonResource(getDAO().save(asSkeletonResourceEntity(skeletonResource)));
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception, ErrorMessages.INSERT_SKELETON_RESOURCE_EXCEPTION);
    }
  }

  @Override
  public Page<SkeletonResource> getAll(Pageable pageable) {
    try {
      Page<SkeletonResourceEntity> skeletonResourceEntityPage = getDAO().findAll(pageable);
      return skeletonResourceEntityPage.map(this::asSkeletonResource);
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception, ErrorMessages.FETCH_SKELETON_RESOURCES_EXCEPTION);
    }
  }

  @Override
  public SkeletonResource update(SkeletonResource skeletonResource) {
    SkeletonResourceEntity skeletonResourceEntityToUpdate = asSkeletonResourceEntity(
        skeletonResource);
    return asSkeletonResource(getSkeletonResourceEntity(skeletonResource.getId()).map(
        skeletonResourceEntityFetched ->
            !skeletonResourceEntityToUpdate.equals(skeletonResourceEntityFetched)
                ? applyUpdateOnRepository(skeletonResourceEntityToUpdate)
                : skeletonResourceEntityFetched).get());
  }

  @Override
  public SkeletonResource patch(SkeletonResource skeletonResource) {

    return asSkeletonResource(
        getSkeletonResourceEntity(skeletonResource.getId()).map(skeletonResourceFetched -> {

          //Creates a new instance to represent the patched object
          SkeletonResourceEntity skeletonResourceEntityToPatch = new SkeletonResourceEntity();
          //Copies current field values from the fetched services.
          modelMapper.map(skeletonResourceFetched, skeletonResourceEntityToPatch);
          //patchMapper only copy non null values.
          //This way, the patched object will reflect the current changes "proposed" by the API.
          patchMapper.map(skeletonResource, skeletonResourceEntityToPatch);
          //The transaction will happen only if there are changes.
          if (!skeletonResourceEntityToPatch.equals(skeletonResourceFetched)) {
            return applyUpdateOnRepository(skeletonResourceEntityToPatch);
          }
          return skeletonResourceFetched;
        }).get());

  }

  @Override
  public boolean existsById(String id) {
    try {
      return getDAO().existsById(id);
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception,
          String.format(ErrorMessages.LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_ID_EXCEPTION, id));
    }
  }

  @Override
  public boolean existsByName(String name) {
    try {
      SkeletonResourceEntity skeletonResourceEntity = getDAO().findIdByName(name);
      return null != skeletonResourceEntity && null != skeletonResourceEntity.getId();
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception,
          String.format(ErrorMessages.LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_NAME_EXCEPTION, name));
    }
  }

  /**
   * Returns the skeletonResource by id
   */
  private Optional<SkeletonResourceEntity> getSkeletonResourceEntity(String skeletonResourceId) {
    try {
      return getDAO().findById(skeletonResourceId);
    } catch (Exception exception) {
      String errorDetail = String
          .format(ErrorMessages.FETCH_BY_ID_SKELETON_RESOURCE_EXCEPTION, skeletonResourceId);
      throw logResolveAndGetThrowable(exception, errorDetail);
    }
  }

  /**
   * Applies the skeletonResource update in the repository
   */
  private SkeletonResourceEntity applyUpdateOnRepository(
      SkeletonResourceEntity skeletonResourceEntity) {
    try {
      //saves the updated skeletonResourceEntity
      return getDAO().save(skeletonResourceEntity);
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception, String
          .format(ErrorMessages.UPDATE_SKELETON_RESOURCE_EXCEPTION,
              skeletonResourceEntity.getId()));
    }
  }

  /**
   * Logs an error message and try to resolve the problem based on know exceptions. Additional
   * information can be passed for the exception message.
   *
   * @param exception captured exception to be resolved by problem helper.
   * @param additionalInfo additional info to prepend to the resolved message.
   * @return A throwable problem based on the parameters provided.
   */
  private ThrowableProblem logResolveAndGetThrowable(Exception exception, String additionalInfo) {
    LOGGER.error(additionalInfo, exception);
    return repositoryProblemHelper.tryResolve(exception, additionalInfo);
  }

  /**
   * Return the specified SkeletonResourceEntity as SkeletonResource
   */
  private SkeletonResource asSkeletonResource(SkeletonResourceEntity skeletonResourceEntity) {
    return modelMapper.map(skeletonResourceEntity, SkeletonResource.class);
  }

  /**
   * Returns the specified SkeletonResource as SkeletonResourceEntity
   */
  private SkeletonResourceEntity asSkeletonResourceEntity(SkeletonResource skeletonResource) {
    return modelMapper.map(skeletonResource, SkeletonResourceEntity.class);
  }
}
