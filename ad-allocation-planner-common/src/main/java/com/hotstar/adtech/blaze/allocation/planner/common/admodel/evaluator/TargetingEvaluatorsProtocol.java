package com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator;

import java.util.BitSet;
import java.util.Map;
import java.util.TreeSet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TargetingEvaluatorsProtocol {

  private final Map<Integer, RuleFeasibleProtocol> audience;
  private final RuleFeasibleProtocol stream;
  private final RuleFeasibleProtocol breakTargeting;
  private final RuleFeasibleProtocol streamNew;
  private final RuleFeasibleProtocol language;
  private final RuleFeasibleProtocol duration;
  private final RuleFeasibleProtocol aspectRatio;
  private final TreeSet<Integer> durationSet;
  private final long[] activeAdSet;
  private final int maxBitIndex;

  public BitSet getActiveAdSetBitSet() {
    return BitSet.valueOf(activeAdSet);
  }
}
