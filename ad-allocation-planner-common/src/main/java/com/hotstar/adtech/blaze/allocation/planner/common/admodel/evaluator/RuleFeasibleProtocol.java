package com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator;

import java.util.BitSet;
import java.util.Collections;
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

  public static RuleFeasibleProtocol empty() {
    return RuleFeasibleProtocol.builder()
      .ignoreInclude(new long[0])
      .invertedIndex(Collections.emptyMap())
      .size(0)
      .build();
  }
}
