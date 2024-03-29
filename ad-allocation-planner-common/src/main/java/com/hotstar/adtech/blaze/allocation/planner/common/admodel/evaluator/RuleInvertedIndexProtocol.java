package com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator;

import java.util.BitSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RuleInvertedIndexProtocol {
  long[] include;
  long[] exclude;
  String tag;

  public BitSet getIncludeBitSet() {
    return BitSet.valueOf(include);
  }

  public BitSet getExcludeBitSet() {
    return BitSet.valueOf(exclude);
  }
}
