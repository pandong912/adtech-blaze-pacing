package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.google.common.collect.Sets;
import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroupFactory;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.BreakTargetingRule;
import java.util.Arrays;
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
    BreakTypeGroupFactory executor =
      new BreakTypeGroupFactory();
    List<BreakTypeGroup> breakTypeGroups =
      executor.getBreakTypeList(buildAdSets(), QualificationTestData.getBreakDetails());
    System.out.println(breakTypeGroups);

    Assertions.assertEquals(3, breakTypeGroups.size());
    System.out.println(breakTypeGroups);

    BreakTypeGroup group = breakTypeGroups.stream()
      .filter(breakTypeGroup -> breakTypeGroup.getBreakTypeIds().size() == 2)
      .findFirst()
      .get();
    Assertions.assertEquals(6, group.getAllBreakDurations().size());
    Assertions.assertTrue(Arrays.asList(30000, 50000, 20000, 40000, 60000, 70000)
      .containsAll(group.getAllBreakDurations()));

  }


  private List<AdSet> buildAdSets() {
    return Arrays.asList(
      AdSet.builder()
        .id(1)
        .breakTargetingRule(BreakTargetingRule.builder()
          .breakTypeIds(Arrays.asList(1, 2))
          .ruleType(RuleType.Include)
          .build())
        .build(),
      AdSet.builder()
        .id(2)
        .breakTargetingRule(BreakTargetingRule.builder()
          .breakTypeIds(Collections.singletonList(2))
          .ruleType(RuleType.Include)
          .build())
        .build()
    );
  }

  private ConcurrencyData getConcurrencyData() {
    return ConcurrencyData.builder()
      .cohorts(Collections.emptyList())
      .streams(Collections.emptyList())
      .build();
  }
}
