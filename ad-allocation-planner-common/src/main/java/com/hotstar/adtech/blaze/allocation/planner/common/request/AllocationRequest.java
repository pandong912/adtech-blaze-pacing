package com.hotstar.adtech.blaze.allocation.planner.common.request;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationRequest {
  String contentId;
  ConcurrencyData concurrencyData;
  AdModelVersion adModelVersion;
  Map<Long, Long> adSetImpressions;
  Integer totalBreakNumber;
  Integer currentBreakIndex;
  List<BreakDetail> breakDetails;
  List<Double> matchBreakProgressRatios;
  PlanType planType;

}
