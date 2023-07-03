package com.hotstar.adtech.blaze.exchanger.controller;

import static com.hotstar.adtech.blaze.exchanger.api.Constant.ALLOCATION_PLAN_PATH;

import com.hotstar.adtech.blaze.admodel.common.domain.ResultCode;
import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AllocationPlanResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AllocationPlanUriResponse;
import com.hotstar.adtech.blaze.exchanger.service.AllocationPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ALLOCATION_PLAN_PATH)
@RequiredArgsConstructor
public class AllocationPlanController {
  private final AllocationPlanService allocationPlanService;

  //for Debug only
  @GetMapping("/debug/{contentId}/{version}")
  public StandardResponse<AllocationPlanResponse> getAllocationPlanResult(
    @PathVariable("contentId") String contentId,
    @PathVariable("version") Long version) {
    return allocationPlanService.getAllocationPlanResult(contentId, version)
      .map(StandardResponse::success)
      .orElseGet(() -> StandardResponse.error(ResultCode.NOT_FOUND,
        "No Allocation Plan found for contentId: " + contentId + " and version: " + version));
  }


  @GetMapping("/match/{contentId}")
  public StandardResponse<AllocationPlanUriResponse> getAllocationPlanUri(
    @PathVariable String contentId,
    @RequestParam Long version) {
    return allocationPlanService.getAllocationPlanUri(contentId, version)
      .map(StandardResponse::success)
      .orElseGet(() -> StandardResponse.error(ResultCode.NOT_FOUND,
        "No Allocation Plan found for contentId: " + contentId + " and version: " + version));
  }
}
