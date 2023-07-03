package com.hotstar.adtech.blaze.allocation.planner.model;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.algomodel.StandardMatchProgressModel;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationContext {
  String contentId;
  ConcurrencyData concurrencyData;
  AdModel adModel;
  Map<Long, Long> adSetImpressions;
  Integer totalBreakNumber;
  Integer currentBreakIndex;
  StandardMatchProgressModel standardMatchProgressModel;
  List<BreakDetail> breakDetails;
  ReachStorage reachStorage;
  AlgorithmType algorithmType;
}
