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
import static org.mockito.Mockito.when;

import com.accessofusion.skeleton.domainmodel.SkeletonResource;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

/**
 * TODO: add meaningful tests  by mocking the dependencies for this service class.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkeletonResourceServiceTestNone {

  private static final String DEFAULT_SKELETON_RESOURCE_ID = "1";

  @Mock
  private SkeletonResourceService skeletonResourceService;

  @Before
  public void setup() {

  }

  @Test
  public void getWithError() {
    String testError = "test error";
    try {
      when(skeletonResourceService.get(anyString()))
          .thenThrow(Problem.valueOf(Status.INTERNAL_SERVER_ERROR, testError));
      skeletonResourceService.get(DEFAULT_SKELETON_RESOURCE_ID);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertEquals(testError, problem.getDetail());
    }
  }

  @Test
  public void getOk() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    when(skeletonResourceService.get(anyString())).thenReturn(skeletonResource);
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

    when(skeletonResourceService.getAll(any())).thenReturn(basePage);

    Page<SkeletonResource> skeletonResourcePage = skeletonResourceService.getAll(pageable);

    assertEquals(basePage.getSize(), skeletonResourcePage.getContent().size());
  }

  @Test
  public void getAllWithError() {

    Pageable pageable = PageRequest.of(0, 5);
    String testError = "test error";

    try {
      when(skeletonResourceService.getAll(any()))
          .thenThrow(Problem.valueOf(Status.INTERNAL_SERVER_ERROR, testError));
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
    when(skeletonResourceService.create(any())).thenReturn(skeletonResource);
    SkeletonResource createdUser = skeletonResourceService.create(skeletonResource);
    assertEquals(createdUser.getId(), skeletonResource.getId());

  }

  @Test
  public void createWithError() {
    SkeletonResource skeletonResource = new SkeletonResource();
    String testError = "test";
    when(skeletonResourceService.create(any()))
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
  public void updateOk() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    when(skeletonResourceService.update(any())).thenReturn(skeletonResource);
    SkeletonResource updatedSkeletonResource = skeletonResourceService.update(skeletonResource);
    assertEquals(updatedSkeletonResource.getId(), skeletonResource.getId());
  }

  @Test
  public void updateWithError() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    String testError = "test";
    when(skeletonResourceService.update(any()))
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
    when(skeletonResourceService.patch(any())).thenReturn(skeletonResource);
    SkeletonResource patchedSkeletonResource = skeletonResourceService.patch(skeletonResource);
    assertEquals(patchedSkeletonResource.getId(), skeletonResource.getId());
  }

  @Test
  public void patchWithError() {
    SkeletonResource skeletonResource = new SkeletonResource();
    skeletonResource.setId(DEFAULT_SKELETON_RESOURCE_ID);
    String testError = "test";
    when(skeletonResourceService.patch(any()))
        .thenThrow(Problem.valueOf(Status.INTERNAL_SERVER_ERROR, testError));
    try {
      skeletonResourceService.patch(skeletonResource);
      fail("An exception was expected");
    } catch (ThrowableProblem problem) {
      assertEquals(Status.INTERNAL_SERVER_ERROR, problem.getStatus());
      assertEquals(testError, problem.getDetail());
    }
  }
}