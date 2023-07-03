package com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HwmAdSetDiagnosis {
  long adSetId;
  int order;
  double demand;
  long target;
  long campaignId;
  long delivered;
  double probability;
  int adDuration;
}
