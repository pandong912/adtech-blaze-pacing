package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortBreakQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
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

    List<ContentCohort> mixedStreamCohorts = concurrency.getCohorts()
      .stream()
      .filter(cohort -> cohort.getStreamType() == StreamType.SSAI_Spot)
      .collect(Collectors.toList());

    List<Request> qualifiedByConcurrency = mixedStreamCohorts.stream()
      .map(contentCohort -> qualifyByConcurrency(contentCohort, adSets,
        generalPlanContext.getAttributeId2TargetingTagMap()))
      .collect(Collectors.toList());


    return breakTypeGroups
      .parallelStream()
      .flatMap(breakTypeGroup -> breakTypeGroup.getAllBreakDurations().parallelStream()
        .map(duration -> buildGraphContextForEachPlan(generalPlanContext, qualifiedByConcurrency, duration,
          breakTypeGroup)))
      .collect(Collectors.toList());
  }


  private GraphContext buildGraphContextForEachPlan(GeneralPlanContext generalPlanContext,
                                                    List<Request> qualifiedByConcurrency,
                                                    Integer duration, BreakTypeGroup breakTypeGroup) {

    List<Request> qualifiedByBreak = qualifiedByConcurrency.stream()
      .map(request -> qualifyByBreak(request, duration))
      .collect(Collectors.toList());

    return GraphContext.builder()
      .breakDuration(duration)
      .breakTypeGroup(breakTypeGroup)
      .planType(PlanType.SSAI)
      .requests(qualifiedByBreak)
      .responses(generalPlanContext.getResponses())
      .build();
  }


  private Request qualifyByConcurrency(ContentCohort contentCohort, List<AdSet> adSets,
                                       Map<String, Integer> attributeId2TargetingTagMap) {
    Integer languageId = contentCohort.getPlayoutStream().getLanguage().getId();
    QualificationEngine<AdSet> qualificationEngine =
      new CohortQualificationEngine(contentCohort.getSsaiTag(), attributeId2TargetingTagMap, languageId);

    List<QualifiedAdSet> qualifiedAdSets = qualificationEngine.qualify(adSets);
    return Request.builder()
      .concurrency(contentCohort.getConcurrency())
      .qualifiedAdSets(qualifiedAdSets)
      .concurrencyId(contentCohort.getConcurrencyId())
      .build();
  }

  private Request qualifyByBreak(Request request, Integer breakDuration) {
    QualificationEngine<QualifiedAdSet> qualificationEngine =
      new CohortBreakQualificationEngine(breakDuration);

    List<QualifiedAdSet> qualifiedAdSets = qualificationEngine.qualify(request.getQualifiedAdSets());
    return Request.builder()
      .concurrencyId(request.getConcurrencyId())
      .concurrency(request.getConcurrency())
      .qualifiedAdSets(qualifiedAdSets)
      .build();
  }

}
