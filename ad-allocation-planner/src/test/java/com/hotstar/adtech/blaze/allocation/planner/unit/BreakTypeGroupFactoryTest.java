package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.hotstar.adtech.blaze.admodel.common.util.BitSetUtil;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.RuleFeasibleProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.RuleInvertedIndexProtocol;
import com.hotstar.adtech.blaze.allocation.planner.qualification.BreakTypeGroupFactory;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakTypeGroup;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BreakTypeGroupFactoryTest {
  public List<BreakTypeGroup> buildBreakTypeGroup() {
    return Arrays.asList(
      BreakTypeGroup.builder()
        .breakTypeIds(Arrays.asList(1, 2, 3))
        .allBreakDurations(Sets.newHashSet(20000, 50000, 80000))
        .build(),
      BreakTypeGroup.builder()
        .breakTypeIds(Collections.singletonList(4))
        .allBreakDurations(Sets.newHashSet(20000, 40000, 50000, 60000, 80000))
        .build()
    );
  }

  @Test
  public void testBreakTypeGroup() {
    // a0 no rule, a1 include 1,2,3,4, a2 exclude1, a3 exclude 3,4
    final BitSet ignoreIncludeBitSet = new BitSet();
    ignoreIncludeBitSet.set(0);
    ignoreIncludeBitSet.set(2);
    ignoreIncludeBitSet.set(3);

    BitSet includeBitSet1 = new BitSet();
    includeBitSet1.set(1);
    BitSet excludeBitSet1 = BitSetUtil.allTrue(4);
    excludeBitSet1.clear(2);
    final RuleInvertedIndexProtocol index1 =
      new RuleInvertedIndexProtocol(includeBitSet1.toLongArray(), excludeBitSet1.toLongArray(), "1");

    BitSet includeBitSet2 = new BitSet();
    includeBitSet2.set(1);
    BitSet excludeBitSet2 = BitSetUtil.allTrue(4);
    final RuleInvertedIndexProtocol index2 =
      new RuleInvertedIndexProtocol(includeBitSet2.toLongArray(), excludeBitSet2.toLongArray(), "2");

    BitSet includeBitSet3 = new BitSet();
    includeBitSet3.set(1);
    BitSet excludeBitSet3 = BitSetUtil.allTrue(4);
    excludeBitSet3.clear(3);
    final RuleInvertedIndexProtocol index3 =
      new RuleInvertedIndexProtocol(includeBitSet3.toLongArray(), excludeBitSet3.toLongArray(), "3");

    BitSet includeBitSet4 = new BitSet();
    includeBitSet4.set(1);
    BitSet excludeBitSet4 = BitSetUtil.allTrue(4);
    excludeBitSet4.clear(3);
    final RuleInvertedIndexProtocol index4 =
      new RuleInvertedIndexProtocol(includeBitSet4.toLongArray(), excludeBitSet4.toLongArray(), "4");

    RuleFeasibleProtocol ruleFeasibleProtocol = RuleFeasibleProtocol.builder()
      .ignoreInclude(ignoreIncludeBitSet.toLongArray())
      .invertedIndex(ImmutableMap.of("1", index1, "2", index2, "3", index3, "4", index4))
      .size(4)
      .build();
    BreakTypeGroupFactory executor = new BreakTypeGroupFactory();
    List<BreakTypeGroup> breakTypeGroups =
      executor.getBreakTypeList(ruleFeasibleProtocol, QualificationTestData.getBreakDetails());
    System.out.println(breakTypeGroups);
    Assertions.assertEquals(3, breakTypeGroups.size());
    Assertions.assertEquals(2, breakTypeGroups.get(0).getBreakTypeIds().size());
  }
}
