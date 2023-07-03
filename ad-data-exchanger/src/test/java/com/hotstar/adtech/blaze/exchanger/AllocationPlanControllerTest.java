package com.hotstar.adtech.blaze.exchanger;

import com.hotstar.adtech.blaze.admodel.common.domain.ResultCode;
import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.exchanger.api.response.AllocationPlanUriResponse;
import com.hotstar.adtech.blaze.exchanger.controller.AllocationPlanController;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
public class AllocationPlanControllerTest extends TestEnvConfig {
  @Autowired
  private AllocationPlanController allocationPlanController;

  @Autowired
  private AllocationPlanResultRepository allocationPlanResultRepository;

  @Test
  public void testGetAllocationPlanResult() {
    AllocationPlanResult allocationPlanResult = AllocationPlanResult.builder()
      .contentId("1540018990")
      .version(Instant.ofEpochMilli(1674035080438L))
      .path("test")
      .allocationPlanResultDetails(Collections.emptyList())
      .build();
    allocationPlanResultRepository.save(allocationPlanResult);
    StandardResponse<AllocationPlanUriResponse> emptyResponse =
      allocationPlanController.getAllocationPlanUri("1540018990", 16740135080438L);
    Assertions.assertEquals(ResultCode.NOT_FOUND, emptyResponse.getCode());
    AllocationPlanUriResponse data =
      allocationPlanController.getAllocationPlanUri("1540018990", 1674035080437L).getData();
    System.out.println(data);
    Assertions.assertEquals("test", data.getPath());
    Assertions.assertEquals(1674035080438L, data.getVersion().longValue());
  }
}
