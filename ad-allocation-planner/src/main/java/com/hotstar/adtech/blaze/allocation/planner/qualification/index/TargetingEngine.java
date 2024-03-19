package com.hotstar.adtech.blaze.allocation.planner.qualification.index;

import com.hotstar.adtech.blaze.admodel.common.util.BitSetUtil;
import java.util.BitSet;
import java.util.Objects;
import java.util.Set;

public class TargetingEngine {
  RuleFeasible feasible;
  int size;

  public TargetingEngine(RuleFeasible feasible) {
    this.feasible = feasible;
    this.size = this.feasible.getSize();
  }

  public BitSet targeting(Set<String> tags) {
    BitSet include = getInclude(tags);
    BitSet exclude = getExclude(tags);
    include.or(feasible.getIngoreIncludeBitSet());
    include.and(exclude);
    return include;
  }

  private BitSet getInclude(Set<String> tags) {
    if (tags.isEmpty()) {
      // if tags is empty, we can only deliver the adSet which doesn't set targeting rule for this category
      // allFalseBitSet OR ignoreInclude is the result of adSet which doesn't any set include rule for this category
      return BitSetUtil.allFalse(size);
    } else {
      return tags.stream()
        .map(feasible::getIndexByTag)
        .map(this::getIncludeBitSet)
        .reduce(BitSetUtil.allTrue(size), BitSetUtil::and);
    }
  }

  private BitSet getIncludeBitSet(RuleInvertedIndex index) {
    return index == null ? BitSetUtil.allFalse(size) : index.getIncludeBitSet();
  }

  private BitSet getExclude(Set<String> tags) {
    if (tags.isEmpty()) {
      // if request does not have tags of this category,
      // we can only deliver the adSet which doesn't set targeting rule for this category
      // here we get the result of adSet which doesn't set any exclude rule
      return feasible.getInvertedIndex().values().stream()
        .map(RuleInvertedIndex::getExcludeBitSet)
        .reduce(BitSetUtil.allTrue(size), BitSetUtil::and);
    } else {
      return tags.stream()
        .map(feasible::getIndexByTag)
        .filter(Objects::nonNull)
        .map(RuleInvertedIndex::getExcludeBitSet)
        .reduce(BitSetUtil.allTrue(size), BitSetUtil::and);
    }
  }
}
