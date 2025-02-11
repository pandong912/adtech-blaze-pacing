package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleResult {
  List<ShaleDemandResult> demandResults;
  List<ShaleSupplyResult> supplyResults;
}
