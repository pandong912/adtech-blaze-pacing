package com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator;

import java.util.BitSet;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RuleFeasibleProtocol {
  private long[] ignoreInclude;
  private Map<String, RuleInvertedIndexProtocol> invertedIndex;
  private int size;

  public BitSet getIgnoreIncludeBitSet() {
    return BitSet.valueOf(ignoreInclude);
  }
}
