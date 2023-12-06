/*
 *
 * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

package com.accessofusion.skeleton.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accessofusion.skeleton.domainmodel.SkeletonResource;
import com.accessofusion.skeleton.problem.ErrorMessages;
import com.accessofusion.skeleton.repository.SkeletonResourceRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@RunWith(MockitoJUnitRunner.class)
public class SkeletonResourceServiceTest {

  private static final String DEFAULT_SKELETON_RESOURCE_ID = "1";

  private SkeletonResourceService skeletonResourceService;

  @Mock
  private SkeletonResourceRepository skeletonResourceRepository;

  @Before
  public void setup() {
    skeletonResourceService = new SkeletonResourceService(skeletonResourceRepository);
  }

  @Test
  public void getWithError() {
    String testError = "test error";
    when(skeletonResourceRepository.existsById(any())).thenReturn(true);
    when(skeletonResourceRepository.get(anyString()))
        .thenThrow(Problem.valueOf(Status.INTERNAL_SERVER_ERROR, testError));
    try {
      skeletonResourceService.get(DEFAULT_SKELETON_RESOURCE_ID);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertEquals(testError, problem.getDetail());
    }
  }

  @Test
  public void getWithNotFound() {
    when(skeletonResourceRepository.existsById(anyString())).thenReturn(false);
    try {
      skeletonResourceService.get(DEFAULT_SKELETON_RESOURCE_ID);
      fail("Exception expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.NOT_FOUND, problem.getStatus());
      assertEquals(String
              .format(ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION, DEFAULT_SKELETON_RESOURCE_ID),
          problem.getDetail());
    }
  }

  @Test
  public void getOk() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    when(skeletonResourceRepository.existsById(anyString())).thenReturn(true);
    when(skeletonResourceRepository.get(anyString())).thenReturn(skeletonResource);
    SkeletonResource fetched = skeletonResourceService.get(DEFAULT_SKELETON_RESOURCE_ID);
    assertEquals(DEFAULT_SKELETON_RESOURCE_ID, fetched.getId());
  }

  @Test
  public void getAllPaged() {
    Pageable pageable = PageRequest.of(0, 5);
    int count = 20;
    List<SkeletonResource> skeletonResources = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      SkeletonResource skeletonResource = new SkeletonResource();
      skeletonResource.setId(Integer.toString(i));
      skeletonResources.add(skeletonResource);
    }
    int start = new Long(pageable.getOffset()).intValue();
    int end = new Long(pageable.getOffset() + pageable.getPageSize()).intValue();
    //Extract  a page from the list
    Page<SkeletonResource> basePage = new PageImpl<>(skeletonResources.subList(start, end),
        pageable,
        skeletonResources.size());

    when(skeletonResourceRepository.getAll(any(Pageable.class))).thenReturn(basePage);

    Page<SkeletonResource> skeletonResourcePage = skeletonResourceService.getAll(pageable);

    assertEquals(basePage.getSize(), skeletonResourcePage.getContent().size());
  }

  @Test
  public void getAllWithError() {

    Pageable pageable = PageRequest.of(0, 5);
    String testError = "test error";
    when(skeletonResourceRepository.getAll(any(Pageable.class)))
        .thenThrow(Problem.valueOf(Status.INTERNAL_SERVER_ERROR, testError));

    HateoasPageableHandlerMethodArgumentResolver resolver = new HateoasPageableHandlerMethodArgumentResolver();
    try {
      skeletonResourceService.getAll(pageable);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertEquals(testError, problem.getDetail());
    }
  }

  @Test
  public void createOk() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);

    when(skeletonResourceRepository.create(any()))
        .thenReturn(skeletonResource);

    SkeletonResource createdUser = skeletonResourceService.create(skeletonResource);
    assertEquals(createdUser.getId(), skeletonResource.getId());

  }

  @Test
  public void createWithError() {
    SkeletonResource skeletonResource = new SkeletonResource();
    String testError = "test";
    when(skeletonResourceRepository.create(any()))
        .thenThrow(Problem.valueOf(Status.INTERNAL_SERVER_ERROR, testError));
    try {
      skeletonResourceService.create(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertEquals(testError, problem.getDetail());
    }
  }

  @Test
  public void createExceptionDuplicateByName() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    skeletonResource.setName("test");
    when(skeletonResourceRepository.existsByName(skeletonResource.getName())).thenReturn(true);
    try {
      skeletonResourceService.create(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.CONFLICT, problem.getStatus());
      assertEquals(String.format(ErrorMessages.INSERT_SKELETON_RESOURCE_DUPLICATE_EXCEPTION,
          ErrorMessages.DUPLICATE_KEY_EXCEPTION, "name", skeletonResource.getName()),
          problem.getDetail());
    }
    verify(skeletonResourceRepository, never()).create(any());
  }

  @Test
  public void updateOk() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);

    when(skeletonResourceRepository.existsById(anyString())).thenReturn(true);

    when(skeletonResourceRepository.update(any())).thenReturn(skeletonResource);

    SkeletonResource updatedSkeletonResource = skeletonResourceService.update(skeletonResource);
    assertEquals(updatedSkeletonResource.getId(), skeletonResource.getId());
  }

  @Test
  public void updateWithNotFound() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    when(skeletonResourceRepository.existsById(anyString())).thenReturn(false);
    try {
      skeletonResourceService.update(skeletonResource);
      fail("An not found exception was expected.");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.NOT_FOUND, problem.getStatus());
      assertEquals(String
              .format(ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION, DEFAULT_SKELETON_RESOURCE_ID),
          problem.getDetail());
    }
  }

  @Test
  public void updateWithError() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    String testError = "test";
    when(skeletonResourceRepository.existsById(any())).thenReturn(true);
    when(skeletonResourceRepository.update(any()))
        .thenThrow(Problem.valueOf(Status.INTERNAL_SERVER_ERROR, testError));
    try {
      skeletonResourceService.update(skeletonResource);
      fail("An internal server error exception was expected.");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertEquals(testError, problem.getDetail());
    }
  }

  @Test
  public void patchOk() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);

    when(skeletonResourceRepository.existsById(any())).thenReturn(true);

    when(skeletonResourceRepository.patch(any())).thenReturn(skeletonResource);

    SkeletonResource patchedSkeletonResource = skeletonResourceService.patch(skeletonResource);
    assertEquals(patchedSkeletonResource.getId(), skeletonResource.getId());
  }

  @Test
  public void patchWithError() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    String testError = "test";
    when(skeletonResourceRepository.existsById(any())).thenReturn(true);
    when(skeletonResourceRepository.patch(any()))
        .thenThrow(Problem.valueOf(Status.INTERNAL_SERVER_ERROR, testError));
    try {
      skeletonResourceService.patch(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertEquals(testError, problem.getDetail());
    }
  }

  @Test
  public void patchWithNotFound() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);

    when(skeletonResourceRepository.existsById(anyString())).thenReturn(false);
    try {
      skeletonResourceService.patch(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.NOT_FOUND, problem.getStatus());
      assertEquals(String
              .format(ErrorMessages.SKELETON_RESOURCE_NOTFOUND_EXCEPTION, DEFAULT_SKELETON_RESOURCE_ID),
          problem.getDetail());
    }
  }
}