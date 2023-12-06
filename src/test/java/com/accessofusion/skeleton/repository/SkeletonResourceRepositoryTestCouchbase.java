/*
 *
 *  (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */
package com.accessofusion.skeleton.repository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accessofusion.multitenancy.config.couchbase.RepositoryService;
import com.accessofusion.skeleton.config.SkeletonConfig;
import com.accessofusion.skeleton.dao.SkeletonResourceDAOCouchbase;
import com.accessofusion.skeleton.datamodel.SkeletonResourceEntity;
import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import com.accessofusion.skeleton.problem.RepositoryProblemHelper;
import com.accessofusion.skeleton.problem.RepositoryProblemHelperImplCouchbase;
import com.accessofusion.skeleton.problem.utils.ProblemUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.couchbase.core.CouchbaseQueryExecutionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@RunWith(MockitoJUnitRunner.class)
public class SkeletonResourceRepositoryTestCouchbase {

  private static final String DEFAULT_TEST_ID = "1";
  private static final String DEFAULT_TEST_NAME = "test";
  @Mock
  private RepositoryService repositoryService;

  @Mock
  private SkeletonResourceDAOCouchbase skeletonResourceDAOCouchbase;

  private SkeletonResourceRepository skeletonResourceRepository;

  private ModelMapper modelMapper;

  @Before
  public void setup() {
    when(repositoryService.getRepository(any())).thenReturn(skeletonResourceDAOCouchbase);

    SkeletonConfig skeletonConfig = new SkeletonConfig(Optional.empty());
    modelMapper = skeletonConfig.getModelMapper();

    RepositoryProblemHelper repositoryProblemHelper = new RepositoryProblemHelperImplCouchbase();
    skeletonResourceRepository = new SkeletonResourceRepositoryImplCouchbase(
        repositoryService, modelMapper, skeletonConfig.getPatchMapper(), repositoryProblemHelper);
  }

  @Test
  public void get() {
    SkeletonResourceEntity skeletonResourceEntity = new SkeletonResourceEntity();
    skeletonResourceEntity.setId(DEFAULT_TEST_ID);

    when(skeletonResourceDAOCouchbase.findById(anyString()))
        .thenReturn(Optional.of(skeletonResourceEntity));

    SkeletonResource skeletonResource = skeletonResourceRepository.get(DEFAULT_TEST_ID);

    assertNotNull(skeletonResource);
    assertEquals(DEFAULT_TEST_ID, skeletonResource.getId());
  }

  @Test
  public void getWithUnexpectedException() {
    String testExceptionMessage = "test exception";
    CouchbaseQueryExecutionException couchbaseQueryExecutionException = new CouchbaseQueryExecutionException(
        testExceptionMessage);
    when(skeletonResourceDAOCouchbase.findById(anyString()))
        .thenThrow(couchbaseQueryExecutionException);

    try {
      skeletonResourceRepository.get(DEFAULT_TEST_ID);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertThat(problem.getMessage(), containsString(
          String.format(ErrorMessages.FETCH_BY_ID_SKELETON_RESOURCE_EXCEPTION, DEFAULT_TEST_ID)));
      assertThat(problem.getMessage(),
          containsString(ErrorMessages.BACKEND_NOT_AVAILABLE_FOR_QUERIES));
    }
  }

  @Test
  public void create() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    when(skeletonResourceDAOCouchbase.save(any()))
        .thenReturn(modelMapper.map(skeletonResource, SkeletonResourceEntity.class));
    assertEquals(skeletonResource, skeletonResourceRepository.create(skeletonResource));
  }

  @Test
  public void createWithDataSourceException() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    CouchbaseQueryExecutionException couchbaseQueryExecutionException = new CouchbaseQueryExecutionException(
        "test error");
    when(skeletonResourceDAOCouchbase.save(any())).thenThrow(couchbaseQueryExecutionException);
    try {
      skeletonResourceRepository.create(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_NOT_AVAILABLE_FOR_QUERIES,
              ErrorMessages.INSERT_SKELETON_RESOURCE_EXCEPTION);
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void getAll() {
    Pageable pageable = PageRequest.of(0, 5);
    int count = 20;
    List<SkeletonResourceEntity> skeletonResources = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      SkeletonResourceEntity skeletonResourceEntity = new SkeletonResourceEntity();
      skeletonResourceEntity.setId(Integer.toString(i));
      skeletonResources.add(skeletonResourceEntity);
    }
    int start = new Long(pageable.getOffset()).intValue();
    int end = new Long(pageable.getOffset() + pageable.getPageSize()).intValue();
    //Extract  a page from the list
    List<SkeletonResourceEntity> sublist = skeletonResources.subList(start, end);
    //Extract  a page from the list
    Page<SkeletonResourceEntity> basePage = new PageImpl<>(sublist, pageable,
        skeletonResources.size());
    when(skeletonResourceDAOCouchbase.findAll(any(Pageable.class))).thenReturn(basePage);

    Page<SkeletonResource> skeletonResourcePage = skeletonResourceRepository.getAll(pageable);

    assertEquals(sublist.size(), skeletonResourcePage.getContent().size());
    //Every item returned in the page should be part of the sublist
    skeletonResourcePage.forEach(skeletonResourceElement -> assertThat(sublist,
        Matchers.hasItem(modelMapper.map(skeletonResourceElement, SkeletonResourceEntity.class))));
  }

  @Test
  public void getAllWithUnexpectedException() {
    Pageable pageable = PageRequest.of(0, 5);
    when(skeletonResourceDAOCouchbase.findAll(any(Pageable.class))).thenThrow(
        new CouchbaseQueryExecutionException("test exception"));

    try {
      skeletonResourceRepository.getAll(pageable);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_NOT_AVAILABLE_FOR_QUERIES,
              ErrorMessages.FETCH_SKELETON_RESOURCES_EXCEPTION);
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void update() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    SkeletonResourceEntity skeletonResourceEntity = modelMapper
        .map(skeletonResource, SkeletonResourceEntity.class);
    skeletonResourceEntity.setActive(false);
    when(skeletonResourceDAOCouchbase.findById(anyString()))
        .thenReturn(Optional.of(skeletonResourceEntity));
    when(skeletonResourceDAOCouchbase.save(any()))
        .thenReturn(modelMapper.map(skeletonResource, SkeletonResourceEntity.class));
    assertEquals(skeletonResource, skeletonResourceRepository.update(skeletonResource));
    verify(skeletonResourceDAOCouchbase, atLeastOnce()).save(any());
  }

  @Test
  public void updateWithNoChanges() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    SkeletonResourceEntity skeletonResourceEntity = modelMapper
        .map(skeletonResource, SkeletonResourceEntity.class);
    when(skeletonResourceDAOCouchbase.findById(anyString()))
        .thenReturn(Optional.of(skeletonResourceEntity));
    assertEquals(skeletonResource, skeletonResourceRepository.update(skeletonResource));
    verify(skeletonResourceDAOCouchbase, never()).save(any());
  }

  @Test
  public void updateWithDataSourceException() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOCouchbase.findById(anyString()))
        .thenReturn(Optional.of(skeletonResourceEntity));
    DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException(
        "test error");
    when(skeletonResourceDAOCouchbase.save(any())).thenThrow(dataIntegrityViolationException);
    try {
      SkeletonResource skeletonResource = modelMapper
          .map(skeletonResourceEntity, SkeletonResource.class);
      skeletonResource.setName("changed");
      skeletonResourceRepository
          .update(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_UNEXPECTED_ERROR,
              String.format(ErrorMessages.UPDATE_SKELETON_RESOURCE_EXCEPTION, DEFAULT_TEST_ID));
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void patch() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOCouchbase.findById(anyString()))
        .thenReturn(Optional.of(skeletonResourceEntity));
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    skeletonResource.setName("changed");
    when(skeletonResourceDAOCouchbase.save(any()))
        .thenReturn(modelMapper.map(skeletonResource, SkeletonResourceEntity.class));
    assertEquals(skeletonResource, skeletonResourceRepository.patch(skeletonResource));
    verify(skeletonResourceDAOCouchbase, atLeastOnce()).save(any());
  }

  @Test
  public void patchWithNoChanges() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOCouchbase.findById(any()))
        .thenReturn(Optional.of(skeletonResourceEntity));
    SkeletonResource skeletonResource = modelMapper
        .map(skeletonResourceEntity, SkeletonResource.class);
    assertEquals(skeletonResource, skeletonResourceRepository.patch(skeletonResource));
    verify(skeletonResourceDAOCouchbase, never()).save(any());
  }

  @Test
  public void patchWithDataSourceException() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOCouchbase.findById(anyString()))
        .thenReturn(Optional.of(skeletonResourceEntity));
    CouchbaseQueryExecutionException couchbaseQueryExecutionException = new CouchbaseQueryExecutionException(
        "test error");
    when(skeletonResourceDAOCouchbase.save(any())).thenThrow(couchbaseQueryExecutionException);
    try {
      SkeletonResource skeletonResource = modelMapper
          .map(skeletonResourceEntity, SkeletonResource.class);
      skeletonResource.setName("changed");
      skeletonResourceRepository
          .patch(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_NOT_AVAILABLE_FOR_QUERIES,
              String.format(ErrorMessages.UPDATE_SKELETON_RESOURCE_EXCEPTION, DEFAULT_TEST_ID));
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void existsById() {
    when(skeletonResourceDAOCouchbase.existsById(anyString())).thenReturn(true);
    boolean result = skeletonResourceRepository.existsById(DEFAULT_TEST_ID);
    assertTrue(result);
  }

  @Test
  public void existsByIdWithUnexpectedException() {
    String testExceptionMessage = "test exception";
    CouchbaseQueryExecutionException couchbaseQueryExecutionException = new CouchbaseQueryExecutionException(
        testExceptionMessage);
    when(skeletonResourceDAOCouchbase.existsById(anyString()))
        .thenThrow(couchbaseQueryExecutionException);

    try {
      skeletonResourceRepository.existsById(DEFAULT_TEST_ID);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertThat(problem.getMessage(), containsString(String
          .format(ErrorMessages.LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_ID_EXCEPTION,
              DEFAULT_TEST_ID)));
      assertThat(problem.getMessage(),
          containsString(ErrorMessages.BACKEND_NOT_AVAILABLE_FOR_QUERIES));
    }
  }

  @Test
  public void existsByName() {
    SkeletonResourceEntity skeletonResourceEntity = new SkeletonResourceEntity();
    skeletonResourceEntity.setId(DEFAULT_TEST_ID);
    when(skeletonResourceDAOCouchbase.findIdByName(anyString())).thenReturn(skeletonResourceEntity);
    boolean result = skeletonResourceRepository.existsByName(DEFAULT_TEST_NAME);
    assertTrue(result);
  }

  @Test
  public void existsByNameWithUnexpectedException() {
    String testExceptionMessage = "test exception";
    CouchbaseQueryExecutionException couchbaseQueryExecutionException = new CouchbaseQueryExecutionException(
        testExceptionMessage);
    when(skeletonResourceDAOCouchbase.findIdByName(anyString()))
        .thenThrow(couchbaseQueryExecutionException);

    try {
      skeletonResourceRepository.existsByName(DEFAULT_TEST_NAME);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertThat(problem.getMessage(), containsString(String
          .format(ErrorMessages.LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_NAME_EXCEPTION,
              DEFAULT_TEST_NAME)));
      assertThat(problem.getMessage(),
          containsString(ErrorMessages.BACKEND_NOT_AVAILABLE_FOR_QUERIES));
    }
  }

  private SkeletonResource getDefaultSkeletonResourceForTest() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_TEST_ID);
    skeletonResource.setName("test");
    skeletonResource.setActive(true);
    return skeletonResource;
  }

  private SkeletonResourceEntity getDefaultSkeletonResourceEntityForTest() {
    return modelMapper.map(getDefaultSkeletonResourceForTest(), SkeletonResourceEntity.class);
  }
}