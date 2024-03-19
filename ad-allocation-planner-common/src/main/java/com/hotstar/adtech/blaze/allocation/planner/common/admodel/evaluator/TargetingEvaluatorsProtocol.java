package com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator;

import java.util.BitSet;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TargetingEvaluatorsProtocol {

  private final Map<Integer, RuleFeasibleProtocol> audience;
  private final RuleFeasibleProtocol stream;
  private final RuleFeasibleProtocol breakTargeting;
  private final RuleFeasibleProtocol streamNew;
  private final long[] activeAdSet;
  private final int adSetSize;

  public BitSet getActiveAdSetBitSet() {
    return BitSet.valueOf(activeAdSet);
  }
}
