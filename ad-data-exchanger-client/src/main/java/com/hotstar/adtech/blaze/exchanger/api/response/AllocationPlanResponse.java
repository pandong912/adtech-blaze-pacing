package com.hotstar.adtech.blaze.exchanger.api.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationPlanResponse {
  List<HwmAllocationPlanResponse> hwmAllocationPlanResponses;
  List<ShaleAllocationPlanResponse> shaleAllocationPlanResponses;
}
