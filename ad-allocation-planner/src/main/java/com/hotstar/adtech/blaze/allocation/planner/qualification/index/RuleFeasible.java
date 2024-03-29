package com.hotstar.adtech.blaze.allocation.planner.qualification.index;

import com.hotstar.adtech.blaze.admodel.common.util.BitSetUtil;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.RuleFeasibleProtocol;
import java.util.BitSet;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleFeasible {
  private BitSet ingoreIncludeBitSet;
  private Map<String, RuleInvertedIndex> invertedIndex;
  private int size;

  public RuleInvertedIndex getIndexByTag(String tag) {
    return invertedIndex.get(tag);
  }

  public static RuleFeasible of(RuleFeasibleProtocol targetingFeasible) {
    BitSet ingoreIncludeBitSet = targetingFeasible.getIgnoreIncludeBitSet();
    Map<String, RuleInvertedIndex> collect = targetingFeasible.getInvertedIndex().values()
      .stream()
      .map(RuleInvertedIndex::of)
      .collect(Collectors.toMap(RuleInvertedIndex::getTag, Function.identity()));
    return new RuleFeasible(ingoreIncludeBitSet, collect, targetingFeasible.getSize());
  }


  public static RuleFeasible of(boolean qualifiedWhenNoInclude, int size) {
    BitSet ignoreIncludeBitSet = qualifiedWhenNoInclude ? BitSetUtil.allTrue(size) : BitSetUtil.allFalse(size);
    return new RuleFeasible(ignoreIncludeBitSet, Collections.emptyMap(), size);
  }

  public static RuleFeasible empty() {
    BitSet ignoreIncludeBitSet = BitSetUtil.allTrue(0);
    return new RuleFeasible(ignoreIncludeBitSet, Collections.emptyMap(), 0);
  }
}
