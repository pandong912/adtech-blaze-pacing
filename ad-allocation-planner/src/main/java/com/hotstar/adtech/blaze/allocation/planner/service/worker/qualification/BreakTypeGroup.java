package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BreakTypeGroup {
  List<Integer> breakTypeIds;
  List<String> breakTypes;
  Set<Integer> allBreakDurations;
}
