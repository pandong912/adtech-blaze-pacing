package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortBreakQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Languages;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SsaiQualificationExecutor implements PlanQualificationExecutor {

  @Override
  @Timed(value = "plan.qualification", extraTags = {"type", "ssai"})
  public List<GraphContext> executeQualify(GeneralPlanContext generalPlanContext) {
    List<AdSet> adSets = generalPlanContext.getAdSets();
    ConcurrencyData concurrency = generalPlanContext.getConcurrencyData();
    Languages languages = generalPlanContext.getLanguages();

    List<ContentCohort> mixedStreamCohorts = concurrency.getCohorts()
      .stream()
      .filter(cohort -> cohort.getStreamType() == StreamType.SSAI_Spot)
      .collect(Collectors.toList());

    List<Request> qualifiedByConcurrency = mixedStreamCohorts.stream()
      .map(contentCohort -> qualifyByConcurrency(contentCohort, adSets,
        generalPlanContext.getAttributeId2TargetingTagMap(), languages))
      .collect(Collectors.toList());

    List<Integer> durationList = getDurationList(generalPlanContext.getBreakDetails());


    return durationList
      .parallelStream()
      .map(breakDuration -> buildGraphContextForEachPlan(generalPlanContext, qualifiedByConcurrency,
        breakDuration))
      .collect(Collectors.toList());
  }

  //todo duration list
  private List<Integer> getDurationList(List<BreakDetail> breakDetails) {
    BreakDetail overBreakDetail = breakDetails.stream()
      .filter(breakDetail -> breakDetail.getBreakTypeId() == 1)
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Over break not found"));
    return overBreakDetail.getBreakDuration();
  }

  private GraphContext buildGraphContextForEachPlan(GeneralPlanContext generalPlanContext,
                                                    List<Request> qualifiedByConcurrency,
                                                    Integer breakDuration) {
    BreakDetail breakDetail = BreakDetail.getEmptyBreakDetail();

    List<Request> qualifiedByBreak = qualifiedByConcurrency.stream()
      .map(request -> qualifyByBreak(request, breakDuration))
      .collect(Collectors.toList());

    return GraphContext.builder()
      .breakDuration(breakDuration)
      .breakDetail(breakDetail)
      .planType(PlanType.SSAI)
      .requests(qualifiedByBreak)
      .responses(generalPlanContext.getResponses())
      .build();
  }


  private Request qualifyByConcurrency(ContentCohort contentCohort, List<AdSet> adSets,
                                       Map<String, Integer> attributeId2TargetingTagMap, Languages languages) {
    QualificationEngine<AdSet> qualificationEngine =
      new CohortQualificationEngine(contentCohort, attributeId2TargetingTagMap, languages);

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
