package com.hotstar.adtech.blaze.allocation.planner.qualification.index;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.RuleInvertedIndexProtocol;
import java.util.BitSet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleInvertedIndex {
  BitSet includeBitSet;
  BitSet excludeBitSet;
  String tag;

  public static RuleInvertedIndex of(RuleInvertedIndexProtocol invertedIndex) {
    BitSet includeBitSet = invertedIndex.getIncludeBitSet();
    BitSet excludeBitSet = invertedIndex.getExcludeBitSet();
    String tag = invertedIndex.getTag();
    return new RuleInvertedIndex(includeBitSet, excludeBitSet, tag);
  }
}
