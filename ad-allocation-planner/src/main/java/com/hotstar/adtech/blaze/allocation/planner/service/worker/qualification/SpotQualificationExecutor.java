package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamBreakQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SpotQualificationExecutor {


  @Timed(value = QUALIFICATION, extraTags = {"type", "spot"})
  public List<GraphContext> executeQualify(GeneralPlanContext generalPlanContext,
                                           List<BreakTypeGroup> breakTypeGroups) {

    List<AdSet> adSets = generalPlanContext.getAdSets();
    ConcurrencyData concurrency = generalPlanContext.getConcurrencyData();

    List<Request> qualifiedByConcurrency = concurrency.getStreams().stream()
      .map(contentStream -> qualifyByConcurrency(contentStream, adSets))
      .collect(Collectors.toList());

    return breakTypeGroups.parallelStream()
      .flatMap(breakTypeGroup -> breakTypeGroup.getAllBreakDurations()
        .parallelStream()
        .map(breakDuration -> buildGraphContextForEachPlan(generalPlanContext, qualifiedByConcurrency, breakTypeGroup,
          breakDuration)))
      .collect(Collectors.toList());

  }

  private GraphContext buildGraphContextForEachPlan(GeneralPlanContext generalPlanContext,
                                                    List<Request> qualifiedByConcurrency, BreakTypeGroup breakTypeGroup,
                                                    Integer breakDuration) {
    List<Request> requests = qualifiedByConcurrency.stream()
      .map(request -> qualifyByBreak(request, breakTypeGroup.getBreakTypeIds().get(0), breakDuration))
      .collect(Collectors.toList());

    return GraphContext.builder()
      .breakDuration(breakDuration)
      .breakTypeGroup(breakTypeGroup)
      .planType(PlanType.SPOT)
      .requests(requests)
      .responses(generalPlanContext.getResponses())
      .build();
  }


  private Request qualifyByConcurrency(ContentStream contentStream, List<AdSet> adSets) {
    QualificationEngine<AdSet> qualificationEngine = new StreamQualificationEngine(contentStream.getPlayoutStream());
    return Request.builder()
      .concurrency(contentStream.getConcurrency())
      .qualifiedAdSets(qualificationEngine.qualify(adSets))
      .concurrencyId(contentStream.getConcurrencyId())
      .build();
  }

  private Request qualifyByBreak(Request request, Integer breakTypeId, Integer breakDuration) {
    QualificationEngine<QualifiedAdSet> qualificationEngine =
      new StreamBreakQualificationEngine(breakDuration, breakTypeId);

    List<QualifiedAdSet> qualifiedAdSets = qualificationEngine.qualify(request.getQualifiedAdSets());
    return Request.builder()
      .concurrencyId(request.getConcurrencyId())
      .concurrency(request.getConcurrency())
      .qualifiedAdSets(qualifiedAdSets)
      .build();
  }
}
