package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HwmAllocationPlanResponse {
  String contentId;
  Integer nextBreak;
  Integer totalBreaks;
  List<Integer> breakTypeIds;
  PlanType planType;
  Integer duration;
  Map<Long, Double> allocationResults;
}
