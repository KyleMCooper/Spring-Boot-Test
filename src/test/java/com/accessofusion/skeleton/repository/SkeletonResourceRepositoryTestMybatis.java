/*
 *
 * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
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

import com.accessofusion.skeleton.config.SkeletonConfig;
import com.accessofusion.skeleton.dao.SkeletonResourceDAOMybatis;
import com.accessofusion.skeleton.datamodel.SkeletonResourceEntity;
import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import com.accessofusion.skeleton.problem.RepositoryProblemHelper;
import com.accessofusion.skeleton.problem.RepositoryProblemHelperImplMybatis;
import com.accessofusion.skeleton.problem.utils.ProblemUtils;
import java.sql.SQLException;
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
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.BadSqlGrammarException;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@RunWith(MockitoJUnitRunner.class)
public class SkeletonResourceRepositoryTestMybatis {

  private static final String DEFAULT_TEST_NAME = "test";
  private static final String DEFAULT_TEST_ID = "1";

  @Mock
  private SkeletonResourceDAOMybatis skeletonResourceDAOMybatis;

  private SkeletonResourceRepository skeletonResourceRepository;

  private ModelMapper modelMapper;


  @Before
  public void setup() {
    SkeletonConfig skeletonConfig = new SkeletonConfig(Optional.empty());
    modelMapper = skeletonConfig.getModelMapper();

    RepositoryProblemHelper repositoryProblemHelper = new RepositoryProblemHelperImplMybatis();
    skeletonResourceRepository = new SkeletonResourceRepositoryImplMybatis(
        skeletonResourceDAOMybatis, modelMapper, skeletonConfig.getPatchMapper(),
        repositoryProblemHelper);
  }

  @Test
  public void get() {
    SkeletonResourceEntity skeletonResourceEntity = new SkeletonResourceEntity();
    skeletonResourceEntity.setId(DEFAULT_TEST_ID);

    when(skeletonResourceDAOMybatis.findById(anyString())).thenReturn(skeletonResourceEntity);

    SkeletonResource skeletonResource = skeletonResourceRepository.get(DEFAULT_TEST_ID);

    assertNotNull(skeletonResource);
    assertEquals(DEFAULT_TEST_ID, skeletonResource.getId());
  }

  @Test
  public void getWithUnexpectedException() {
    String testExceptionMessage = "test exception";
    DataRetrievalFailureException dataRetrievalFailureException = new DataRetrievalFailureException(
        testExceptionMessage);
    when(skeletonResourceDAOMybatis.findById(anyString())).thenThrow(dataRetrievalFailureException);

    try {
      skeletonResourceRepository.get(DEFAULT_TEST_ID);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.CONFLICT, problem.getStatus());
      assertThat(problem.getMessage(), containsString(
          String.format(ErrorMessages.FETCH_BY_ID_SKELETON_RESOURCE_EXCEPTION, DEFAULT_TEST_ID)));
      assertThat(problem.getMessage(), containsString(ErrorMessages.DATA_ACCESS_EXCEPTION));
    }
  }

  @Test
  public void create() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    when(skeletonResourceDAOMybatis.insert(any())).thenReturn(1);
    assertEquals(skeletonResource, skeletonResourceRepository.create(skeletonResource));
  }

  @Test
  public void createWithDuplicatedKeyException() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    when(skeletonResourceDAOMybatis.insert(any()))
        .thenThrow(new DuplicateKeyException("test error"));
    try {
      skeletonResourceRepository.create(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.CONFLICT, ErrorMessages.DUPLICATE_KEY_EXCEPTION,
              ErrorMessages.INSERT_SKELETON_RESOURCE_EXCEPTION);
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void createWithNoRecordsAffected() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    when(skeletonResourceDAOMybatis.insert(any())).thenReturn(0);
    try {
      skeletonResourceRepository.create(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.CONFLICT, problem.getStatus());
      assertEquals(ErrorMessages.SKELETON_RESOURCE_NOT_INSERTED, problem.getDetail());
    }
  }

  @Test
  public void createWithUnexpectedException() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    when(skeletonResourceDAOMybatis.insert(any())).thenThrow(new RuntimeException("test error"));
    try {
      skeletonResourceRepository.create(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_UNEXPECTED_ERROR,
              ErrorMessages.INSERT_SKELETON_RESOURCE_EXCEPTION);
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void createWithDataSourceException() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException(
        "test error");
    when(skeletonResourceDAOMybatis.insert(any())).thenThrow(dataIntegrityViolationException);
    try {
      skeletonResourceRepository.create(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.CONFLICT, problem.getStatus());
      assertThat(problem.getMessage(), containsString(ErrorMessages.DATA_INTEGRITY_EXCEPTION));
      assertThat(problem.getMessage(),
          containsString(ErrorMessages.INSERT_SKELETON_RESOURCE_EXCEPTION));
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

    when(skeletonResourceDAOMybatis.findAll(any(Pageable.class))).thenReturn(sublist);

    Page<SkeletonResource> skeletonResourcePage = skeletonResourceRepository.getAll(pageable);

    assertEquals(sublist.size(), skeletonResourcePage.getContent().size());
    //Every item returned in the page should be part of the sublist
    skeletonResourcePage.forEach(skeletonResourceElement -> assertThat(sublist,
        Matchers.hasItem(modelMapper.map(skeletonResourceElement, SkeletonResourceEntity.class))));
  }

  @Test
  public void getAllWithBadSqlGrammarException() {
    String sortByField = "testField";
    Sort sort = Sort.by(sortByField);
    Pageable pageable = PageRequest.of(0, 5, sort);
    when(skeletonResourceDAOMybatis.findAll(any(Pageable.class))).thenThrow(
        new BadSqlGrammarException("test exception", "sql",
            new SQLException("test sql exception")));

    try {
      skeletonResourceRepository.getAll(pageable);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertEquals(String.format(ErrorMessages.SQL_SORT_FIELD_VALUE_ERROR, pageable.getSort()),
          problem.getDetail());
    }
  }


  @Test
  public void getAllWithUnexpectedException() {
    Pageable pageable = PageRequest.of(0, 5);
    when(skeletonResourceDAOMybatis.findAll(any(Pageable.class))).thenThrow(
        new DataRetrievalFailureException("test exception"));

    try {
      skeletonResourceRepository.getAll(pageable);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.CONFLICT, ErrorMessages.DATA_ACCESS_EXCEPTION,
              ErrorMessages.FETCH_SKELETON_RESOURCES_EXCEPTION);
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void getAllWithInternalCountCallException() {
    Pageable pageable = PageRequest.of(0, 5);
    when(skeletonResourceDAOMybatis.count()).thenThrow(
        new DataRetrievalFailureException("test exception"));

    try {
      skeletonResourceRepository.getAll(pageable);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.INTERNAL_SERVER_ERROR, ErrorMessages.BACKEND_UNEXPECTED_ERROR,
              ErrorMessages.FETCH_SKELETON_RESOURCES_EXCEPTION);
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void updateWithNotFoundException() {
    SkeletonResourceEntity skeletonResourceEntity = new SkeletonResourceEntity();
    skeletonResourceEntity.setId(DEFAULT_TEST_ID);
    when(skeletonResourceDAOMybatis.findById(anyString())).thenReturn(null);
    try {
      SkeletonResource skeletonResource = modelMapper
          .map(skeletonResourceEntity, SkeletonResource.class);
      skeletonResourceRepository.update(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.NOT_FOUND, problem.getStatus());
      assertEquals(
          String.format(ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION, DEFAULT_TEST_ID),
          problem.getDetail());
    }
  }

  @Test
  public void update() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    SkeletonResourceEntity skeletonResourceEntity = modelMapper
        .map(skeletonResource, SkeletonResourceEntity.class);
    skeletonResourceEntity.setActive(false);
    when(skeletonResourceDAOMybatis.findById(anyString()))
        .thenReturn(skeletonResourceEntity);
    when(skeletonResourceDAOMybatis.update(any())).thenReturn(1);
    assertEquals(skeletonResource, skeletonResourceRepository.update(skeletonResource));
    verify(skeletonResourceDAOMybatis, atLeastOnce()).update(any());
  }

  @Test
  public void updateWithNoChanges() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    SkeletonResourceEntity skeletonResourceEntity = modelMapper
        .map(skeletonResource, SkeletonResourceEntity.class);
    when(skeletonResourceDAOMybatis.findById(anyString()))
        .thenReturn(skeletonResourceEntity);
    assertEquals(skeletonResource, skeletonResourceRepository.update(skeletonResource));
    verify(skeletonResourceDAOMybatis, never()).update(any());
  }

  @Test
  public void updateWithNoRecordsAffectedResult() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOMybatis.findById(any())).thenReturn(skeletonResourceEntity);
    when(skeletonResourceDAOMybatis.update(any())).thenReturn(0);
    SkeletonResource skeletonResource = modelMapper
        .map(skeletonResourceEntity, SkeletonResource.class);
    skeletonResource.setName("changed");
    assertEquals(skeletonResource, skeletonResourceRepository.update(skeletonResource));
    verify(skeletonResourceDAOMybatis, atLeastOnce()).update(any());
  }


  @Test
  public void updateWithTooManyRecordsAffectedResult() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOMybatis.findById(any())).thenReturn(skeletonResourceEntity);
    when(skeletonResourceDAOMybatis.update(any())).thenReturn(2);
    SkeletonResource skeletonResource = modelMapper
        .map(skeletonResourceEntity, SkeletonResource.class);
    skeletonResource.setName("changed");
    assertEquals(skeletonResource, skeletonResourceRepository.update(skeletonResource));
    verify(skeletonResourceDAOMybatis, atLeastOnce()).update(any());
  }

  @Test
  public void updateWithDataSourceException() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOMybatis.findById(anyString()))
        .thenReturn(skeletonResourceEntity);
    DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException(
        "test error");
    when(skeletonResourceDAOMybatis.update(any())).thenThrow(dataIntegrityViolationException);
    try {
      SkeletonResource skeletonResource = modelMapper
          .map(skeletonResourceEntity, SkeletonResource.class);
      skeletonResource.setName("changed");
      skeletonResourceRepository
          .update(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.CONFLICT, ErrorMessages.DATA_INTEGRITY_EXCEPTION,
              String.format(ErrorMessages.UPDATE_SKELETON_RESOURCE_EXCEPTION, DEFAULT_TEST_ID));
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void patch() {
    SkeletonResource skeletonResource = getDefaultSkeletonResourceForTest();
    SkeletonResourceEntity skeletonResourceEntity = modelMapper
        .map(skeletonResource, SkeletonResourceEntity.class);
    skeletonResourceEntity.setActive(false);
    when(skeletonResourceDAOMybatis.findById(anyString()))
        .thenReturn(skeletonResourceEntity);
    when(skeletonResourceDAOMybatis.update(any())).thenReturn(1);
    assertEquals(skeletonResource, skeletonResourceRepository.patch(skeletonResource));
    verify(skeletonResourceDAOMybatis, atLeastOnce()).update(any());
  }

  @Test
  public void patchWithNotFoundException() {
    SkeletonResourceEntity skeletonResourceEntity = new SkeletonResourceEntity();
    skeletonResourceEntity.setId(DEFAULT_TEST_ID);
    when(skeletonResourceDAOMybatis.findById(anyString())).thenReturn(null);
    try {
      SkeletonResource skeletonResource = modelMapper
          .map(skeletonResourceEntity, SkeletonResource.class);
      skeletonResourceRepository.patch(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.NOT_FOUND, problem.getStatus());
      assertEquals(
          String.format(ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION, DEFAULT_TEST_ID),
          problem.getDetail());
    }
  }

  @Test
  public void patchWithNoRecordsAffectedResult() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOMybatis.findById(any())).thenReturn(skeletonResourceEntity);
    when(skeletonResourceDAOMybatis.update(any())).thenReturn(0);
    SkeletonResource skeletonResource = modelMapper
        .map(skeletonResourceEntity, SkeletonResource.class);
    skeletonResource.setName("changed");
    assertEquals(skeletonResource, skeletonResourceRepository.patch(skeletonResource));
    verify(skeletonResourceDAOMybatis, atLeastOnce()).update(any());
  }

  @Test
  public void patchWithNoChanges() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOMybatis.findById(any())).thenReturn(skeletonResourceEntity);
    SkeletonResource skeletonResource = modelMapper
        .map(skeletonResourceEntity, SkeletonResource.class);
    assertEquals(skeletonResource, skeletonResourceRepository.patch(skeletonResource));
    verify(skeletonResourceDAOMybatis, never()).update(any());
  }

  @Test
  public void patchWithDataSourceException() {
    SkeletonResourceEntity skeletonResourceEntity = getDefaultSkeletonResourceEntityForTest();
    when(skeletonResourceDAOMybatis.findById(anyString()))
        .thenReturn(skeletonResourceEntity);
    DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException(
        "test error");
    when(skeletonResourceDAOMybatis.update(any())).thenThrow(dataIntegrityViolationException);
    try {
      SkeletonResource skeletonResource = modelMapper
          .map(skeletonResourceEntity, SkeletonResource.class);
      skeletonResource.setName("changed");
      skeletonResourceRepository
          .patch(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      Problem expected = ProblemUtils
          .getProblem(Status.CONFLICT, ErrorMessages.DATA_INTEGRITY_EXCEPTION,
              String.format(ErrorMessages.UPDATE_SKELETON_RESOURCE_EXCEPTION, DEFAULT_TEST_ID));
      assertEquals(expected.getStatus(), problem.getStatus());
      assertEquals(expected.getDetail(), problem.getDetail());
    }
  }

  @Test
  public void existsById() {
    when(skeletonResourceDAOMybatis.existsById(anyString())).thenReturn(true);
    boolean result = skeletonResourceRepository.existsById(DEFAULT_TEST_ID);
    assertTrue(result);
  }

  @Test
  public void existsByIdWithUnexpectedException() {
    String testExceptionMessage = "test exception";
    DataRetrievalFailureException dataRetrievalFailureException = new DataRetrievalFailureException(
        testExceptionMessage);
    when(skeletonResourceDAOMybatis.existsById(anyString()))
        .thenThrow(dataRetrievalFailureException);

    try {
      skeletonResourceRepository.existsById(DEFAULT_TEST_ID);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.CONFLICT, problem.getStatus());
      assertThat(problem.getMessage(), containsString(String
          .format(ErrorMessages.LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_ID_EXCEPTION,
              DEFAULT_TEST_ID)));
      assertThat(problem.getMessage(), containsString(ErrorMessages.DATA_ACCESS_EXCEPTION));
    }
  }

  @Test
  public void existsByName() {
    when(skeletonResourceDAOMybatis.findIdByName(anyString())).thenReturn(DEFAULT_TEST_ID);
    boolean result = skeletonResourceRepository.existsByName(DEFAULT_TEST_NAME);
    assertTrue(result);
  }

  @Test
  public void existsByNameWithUnexpectedException() {
    String testExceptionMessage = "test exception";
    DataRetrievalFailureException dataRetrievalFailureException = new DataRetrievalFailureException(
        testExceptionMessage);
    when(skeletonResourceDAOMybatis.findIdByName(anyString()))
        .thenThrow(dataRetrievalFailureException);

    try {
      skeletonResourceRepository.existsByName(DEFAULT_TEST_NAME);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.CONFLICT, problem.getStatus());
      assertThat(problem.getMessage(), containsString(String
          .format(ErrorMessages.LOOKING_IF_SKELETON_RESOURCE_EXISTS_BY_NAME_EXCEPTION,
              DEFAULT_TEST_NAME)));
      assertThat(problem.getMessage(), containsString(ErrorMessages.DATA_ACCESS_EXCEPTION));
    }
  }

  private SkeletonResource getDefaultSkeletonResourceForTest() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_TEST_ID);
    skeletonResource.setName(DEFAULT_TEST_NAME);
    skeletonResource.setActive(true);
    return skeletonResource;
  }

  private SkeletonResourceEntity getDefaultSkeletonResourceEntityForTest() {
    return modelMapper.map(getDefaultSkeletonResourceForTest(), SkeletonResourceEntity.class);
  }

}