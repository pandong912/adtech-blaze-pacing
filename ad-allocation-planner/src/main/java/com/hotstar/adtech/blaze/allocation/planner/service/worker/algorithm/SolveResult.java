package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SolveResult {
  @Builder.Default
  List<ShaleResult> shaleResults = new ArrayList<>();
  @Builder.Default
  List<HwmResult> hwmResults = new ArrayList<>();
}
