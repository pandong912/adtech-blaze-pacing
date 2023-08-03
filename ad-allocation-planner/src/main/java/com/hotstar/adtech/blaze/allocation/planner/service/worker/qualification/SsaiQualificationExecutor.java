package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortAdQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortAdSetQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import io.micrometer.core.annotation.Timed;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SsaiQualificationExecutor {

  @Timed(value = QUALIFICATION, extraTags = {"type", "ssai"})
  public List<GraphContext> executeQualify(GeneralPlanContext generalPlanContext,
                                           List<BreakTypeGroup> breakTypeGroups) {
    List<AdSet> adSets = generalPlanContext.getAdSets();
    ConcurrencyData concurrency = generalPlanContext.getConcurrencyData();
    List<ContentCohort> mixedStreamCohorts = concurrency.getCohorts();


    BitSet firstQualified = new BitSet(MemoryAlignment.getSize(adSets) * mixedStreamCohorts.size());
    mixedStreamCohorts
      .parallelStream()
      .forEach(contentCohort -> qualifyByConcurrency(contentCohort, adSets,
        generalPlanContext.getAttributeId2TargetingTagMap(), firstQualified));

    return breakTypeGroups
      .parallelStream()
      .flatMap(breakTypeGroup -> breakTypeGroup.getAllBreakDurations().parallelStream()
        .map(duration -> buildGraphContextForEachPlan(generalPlanContext, firstQualified, duration,
          breakTypeGroup)))
      .collect(Collectors.toList());
  }


  private GraphContext buildGraphContextForEachPlan(GeneralPlanContext generalPlanContext,
                                                    BitSet firstQualified,
                                                    Integer duration, BreakTypeGroup breakTypeGroup) {
    BitSet secondQualified = new BitSet(firstQualified.size());
    List<AdSet> adSets = generalPlanContext.getAdSets();
    generalPlanContext.getConcurrencyData().getCohorts()
      .parallelStream()
      .forEach(request -> qualifyByBreak(request, adSets, firstQualified, secondQualified, duration));

    return GraphContext.builder()
      .breakDuration(duration)
      .breakTypeGroup(breakTypeGroup)
      .planType(PlanType.SSAI)
      .requests(generalPlanContext.getSsaiAndSpotRequests())
      .edges(secondQualified)
      .responses(generalPlanContext.getResponses())
      .build();
  }


  private void qualifyByConcurrency(ContentCohort contentCohort, List<AdSet> adSets,
                                    Map<String, Integer> attributeId2TargetingTagMap, BitSet firstQualified) {

    CohortAdSetQualificationEngine qualificationEngine =
      new CohortAdSetQualificationEngine(contentCohort.getSsaiTag(), attributeId2TargetingTagMap,
        contentCohort.getConcurrencyId(), firstQualified);

    qualificationEngine.qualify(adSets);
  }

  private void qualifyByBreak(ContentCohort request, List<AdSet> adSets, BitSet firstQualified, BitSet secondQualified,
                              Integer breakDuration) {
    Integer languageId = request.getPlayoutStream().getLanguage().getId();
    CohortAdQualificationEngine qualificationEngine =
      new CohortAdQualificationEngine(breakDuration, languageId, request.getConcurrencyId(), firstQualified,
        secondQualified);

    qualificationEngine.qualify(adSets);
  }
}
