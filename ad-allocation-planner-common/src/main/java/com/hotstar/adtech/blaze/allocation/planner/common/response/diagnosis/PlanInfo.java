package com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PlanInfo {
  PlanType planType;
  List<Integer> breakTypeId;
  List<String> breakType;
  int breakDuration;
  int nextBreakIndex;
  int totalBreakNumber;
  int estimatedModelBreakIndex;
  double expectedRatio;
  double expectedProgress;
}
