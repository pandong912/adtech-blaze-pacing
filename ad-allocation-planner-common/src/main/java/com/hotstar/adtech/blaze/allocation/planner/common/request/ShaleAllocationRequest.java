package com.hotstar.adtech.blaze.allocation.planner.common.request;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleAllocationRequest {
  AllocationRequest allocationRequest;
  List<UnReachData> unReachDataList;
  Set<Long> reachAdSetIds;
  double penalty;
}
