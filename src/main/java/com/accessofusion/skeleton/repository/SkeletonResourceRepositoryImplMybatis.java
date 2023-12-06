package com.accessofusion.skeleton.repository;

import com.accessofusion.skeleton.dao.SkeletonResourceDAOMybatis;
import com.accessofusion.skeleton.datamodel.SkeletonResourceEntity;
import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import com.accessofusion.skeleton.problem.RepositoryProblemHelper;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Repository;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@Repository
public class SkeletonResourceRepositoryImplMybatis implements SkeletonResourceRepository {

  private final static Logger LOGGER = LoggerFactory
      .getLogger(SkeletonResourceRepositoryImplMybatis.class);

  private final SkeletonResourceDAOMybatis skeletonResourceDAOMybatis;

  private final ModelMapper modelMapper;
  private final ModelMapper patchMapper;

  private final RepositoryProblemHelper repositoryProblemHelper;

  public SkeletonResourceRepositoryImplMybatis(
      SkeletonResourceDAOMybatis skeletonResourceDAOMybatis,
      @Qualifier("modelMapper") ModelMapper modelMapper,
      @Qualifier("patchMapper") ModelMapper patchMapper,
      RepositoryProblemHelper repositoryProblemHelper) {
    this.skeletonResourceDAOMybatis = skeletonResourceDAOMybatis;
    this.modelMapper = modelMapper;
    this.patchMapper = patchMapper;
    this.repositoryProblemHelper = repositoryProblemHelper;
  }

  @Override
  public SkeletonResource get(String skeletonResourceId) {
    return asSkeletonResource(findSkeletonResourceEntityById(skeletonResourceId));
  }

  @Override
  public SkeletonResource create(SkeletonResource skeletonResource) {
    int insertResult;

    try {
      insertResult = skeletonResourceDAOMybatis
          .insert(asSkeletonResourceEntity(skeletonResource));
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception, ErrorMessages.INSERT_SKELETON_RESOURCE_EXCEPTION);
    }

    if (insertResult > 0) {
      return skeletonResource;
    } else {
      throw logAndGetThrowable(Status.CONFLICT, ErrorMessages.SKELETON_RESOURCE_NOT_INSERTED);
    }
  }

  @Override
  public Page<SkeletonResource> getAll(Pageable pageable) {
    try {
      return new PageImpl<>(
          skeletonResourceDAOMybatis.findAll(pageable).stream().map(this::asSkeletonResource)
              .collect(Collectors.toList()), pageable, getTotalCount());
    } catch (BadSqlGrammarException badSqlGrammarException) {
      throw logAndGetThrowable(Status.INTERNAL_SERVER_ERROR, String.format(
          ErrorMessages.SQL_SORT_FIELD_VALUE_ERROR, pageable.getSort()));
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception, ErrorMessages.FETCH_SKELETON_RESOURCES_EXCEPTION);
    }
  }

  @Override
  public SkeletonResource update(SkeletonResource skeletonResource) {
    SkeletonResourceEntity skeletonResourceEntityFetched = findSkeletonResourceEntityById(
        skeletonResource.getId());
    if (null != skeletonResourceEntityFetched) {
      SkeletonResourceEntity skeletonResourceEntityToUpdate = asSkeletonResourceEntity(
          skeletonResource);
      return asSkeletonResource(
          (!skeletonResourceEntityToUpdate.equals(skeletonResourceEntityFetched))
              ? applyUpdateOnRepository(skeletonResourceEntityToUpdate)
              : skeletonResourceEntityFetched);
    }
    throw logAndGetThrowable(Status.NOT_FOUND, String
        .format(ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION, skeletonResource.getId()));
  }

  @Override
  public SkeletonResource patch(SkeletonResource skeletonResource) {
    SkeletonResourceEntity skeletonResourceEntityFetched = findSkeletonResourceEntityById(
        skeletonResource.getId());
    if (null != skeletonResourceEntityFetched) {
      //Creates a new instance to represent the patched object
      SkeletonResourceEntity patchedEntity = new SkeletonResourceEntity();
      //Copies current field values from the fetched services.
      modelMapper.map(skeletonResourceEntityFetched, patchedEntity);
      //patchMapper only copy non null values.
      //This way, the patched object will reflect the current changes "proposed" by the API.
      patchMapper.map(skeletonResource, patchedEntity);
      //The transaction will happen only if there are changes.
      return asSkeletonResource(
          (!patchedEntity.equals(skeletonResourceEntityFetched)) ? applyUpdateOnRepository(
              patchedEntity) : skeletonResourceEntityFetched);
    }
    throw logAndGetThrowable(Status.NOT_FOUND, String
        .format(ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION, skeletonResource.getId()));
  }

  @Override
  public boolean existsById(String id) {
    try {
      return skeletonResourceDAOMybatis.existsById(id);
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception,
          String.format(ErrorMessages.LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_ID_EXCEPTION, id));
    }
  }

  @Override
  public boolean existsByName(String name) {
    try {
      return null != skeletonResourceDAOMybatis.findIdByName(name);
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception,
          String.format(ErrorMessages.LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_NAME_EXCEPTION, name));
    }
  }

  /**
   * Applies the skeletonResource update in the repository
   */
  private SkeletonResourceEntity applyUpdateOnRepository(SkeletonResourceEntity skeletonResource) {

    int updateResult;
    try {
      updateResult = skeletonResourceDAOMybatis
          .update(skeletonResource);
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception, String
          .format(ErrorMessages.UPDATE_SKELETON_RESOURCE_EXCEPTION, skeletonResource.getId()));
    }

    if (updateResult == 0) {
      LOGGER.warn("SkeletonResource with id {} not updated. No records affected",
          skeletonResource.getId());
    } else if (updateResult > 1) {
      LOGGER.warn("SkeletonResource with id {} updated. Too many records affected {}",
          skeletonResource.getId(), updateResult);
    }
    return skeletonResource;
  }

  /**
   * Finds an skeletonResource by its id
   */
  private SkeletonResourceEntity findSkeletonResourceEntityById(String skeletonResourceId) {
    try {
      return skeletonResourceDAOMybatis.findById(skeletonResourceId);
    } catch (Exception exception) {
      String errorDetail = String
          .format(ErrorMessages.FETCH_BY_ID_SKELETON_RESOURCE_EXCEPTION, skeletonResourceId);
      throw logResolveAndGetThrowable(exception, errorDetail);
    }
  }

  /**
   * Get the count of skeletonResource records
   */
  private long getTotalCount() {
    try {
      return skeletonResourceDAOMybatis.count();
    } catch (Exception exception) {
      throw logResolveAndGetThrowable(exception, ErrorMessages.COUNT_SKELETON_RESOURCES_EXCEPTION);
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
