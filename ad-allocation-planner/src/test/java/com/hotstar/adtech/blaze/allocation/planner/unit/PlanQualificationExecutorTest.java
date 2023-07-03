package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.google.common.collect.Sets;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.SpotQualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.SsaiQualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.BreakTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlanQualificationExecutorTest {
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
  public void testSsai() {
    SsaiQualificationExecutor ssaiQualificationExecutor = new SsaiQualificationExecutor();
    GeneralPlanContext generalPlanContext = getGeneralPlanContext();
    List<GraphContext> graphContexts =
      ssaiQualificationExecutor.executeQualify(generalPlanContext, buildBreakTypeGroup());
    System.out.println(graphContexts);
    Assertions.assertEquals(8, graphContexts.size());
    Assertions.assertEquals(3, graphContexts.get(0).getBreakTypeGroup().getBreakTypeIds().size());
    Assertions.assertEquals(80000, graphContexts.get(2).getBreakDuration());
    Assertions.assertEquals(1, graphContexts.get(4).getBreakTypeGroup().getBreakTypeIds().size());
    Assertions.assertEquals(20000, graphContexts.get(0).getBreakDuration());
    Assertions.assertEquals(50000, graphContexts.get(1).getBreakDuration());
    Assertions.assertEquals(80000, graphContexts.get(2).getBreakDuration());
  }

  @Test
  public void testSpot() {
    SpotQualificationExecutor spotQualificationExecutor = new SpotQualificationExecutor();
    GeneralPlanContext generalPlanContext = getGeneralPlanContext();
    List<GraphContext> graphContexts =
      spotQualificationExecutor.executeQualify(generalPlanContext, buildBreakTypeGroup());
    Assertions.assertEquals(8, graphContexts.size());
    Assertions.assertTrue(
      graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakTypeGroup().getBreakTypeIds().size() == 3));
    Assertions.assertTrue(
      graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakTypeGroup().getBreakTypeIds().size() == 1));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 20000));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 40000));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 50000));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 60000));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 80000));
  }

  @Test
  public void testBreakTypeGroup() {
    QualificationExecutor executor =
      new QualificationExecutor(new SpotQualificationExecutor(), new SsaiQualificationExecutor());
    List<GraphContext> graphContexts = executor.doQualification(PlanType.SSAI, getGeneralPlanContext());
    System.out.println(graphContexts);

    Set<BreakTypeGroup> breakTypeGroupSet =
      graphContexts.stream().map(GraphContext::getBreakTypeGroup).collect(Collectors.toSet());
    Assertions.assertEquals(3, breakTypeGroupSet.size());
    System.out.println(breakTypeGroupSet);

    List<GraphContext> group1 = graphContexts.stream()
      .filter(graphContext -> graphContext.getBreakTypeGroup().getBreakTypeIds().size() == 2)
      .collect(Collectors.toList());
    Assertions.assertEquals(6, group1.size());
    Assertions.assertTrue(Arrays.asList(30000, 50000, 20000, 40000, 60000, 70000)
      .containsAll(group1.stream().map(GraphContext::getBreakDuration).collect(Collectors.toList())));

  }

  public GeneralPlanContext getGeneralPlanContext() {
    return GeneralPlanContext.builder()
      .concurrencyData(getConcurrencyData())
      .breakDetails(QualificationTestData.getBreakDetails())
      .adSets(buildAdSets())
      .attributeId2TargetingTagMap(QualificationTestData.getAttributeId2TargetingTagMap())
      .build();
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
