package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.SpotQualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.SsaiQualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlanQualificationExecutorTest {
  @Test
  public void testSsai() {
    SsaiQualificationExecutor ssaiQualificationExecutor = new SsaiQualificationExecutor();
    GeneralPlanContext generalPlanContext = getGeneralPlanContext();
    List<GraphContext> graphContexts = ssaiQualificationExecutor.executeQualify(generalPlanContext);
    Assertions.assertEquals(3, graphContexts.size());
    Assertions.assertEquals(20000, graphContexts.get(0).getBreakDuration());
    Assertions.assertEquals(50000, graphContexts.get(1).getBreakDuration());
    Assertions.assertEquals(80000, graphContexts.get(2).getBreakDuration());
  }

  @Test
  public void testSpot() {
    SpotQualificationExecutor spotQualificationExecutor = new SpotQualificationExecutor();
    GeneralPlanContext generalPlanContext = getGeneralPlanContext();
    List<GraphContext> graphContexts = spotQualificationExecutor.executeQualify(generalPlanContext);
    Assertions.assertEquals(9, graphContexts.size());
    Assertions.assertTrue(
      graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDetail().getBreakTypeId() == 1));
    Assertions.assertTrue(
      graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDetail().getBreakTypeId() == 2));
    Assertions.assertTrue(
      graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDetail().getBreakTypeId() == 3));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 20000));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 40000));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 50000));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 60000));
    Assertions.assertTrue(graphContexts.stream().anyMatch(graphContext -> graphContext.getBreakDuration() == 80000));
  }

  public GeneralPlanContext getGeneralPlanContext() {
    return GeneralPlanContext.builder()
      .concurrencyData(getConcurrencyData())
      .breakDetails(QualificationTestData.getBreakDetails())
      .adSets(Collections.emptyList())
      .languages(QualificationTestData.getLanguages())
      .attributeId2TargetingTagMap(QualificationTestData.getAttributeId2TargetingTagMap())
      .build();
  }

  private ConcurrencyData getConcurrencyData() {
    return ConcurrencyData.builder()
      .cohorts(Collections.emptyList())
      .streams(Collections.emptyList())
      .build();
  }
}
