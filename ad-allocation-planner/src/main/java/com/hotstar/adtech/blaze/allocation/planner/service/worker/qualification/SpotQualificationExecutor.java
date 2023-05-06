package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamBreakQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Languages;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SpotQualificationExecutor implements PlanQualificationExecutor {


  @Override
  @Timed(value = "plan.qualification", extraTags = {"type", "spot"})
  public List<GraphContext> executeQualify(GeneralPlanContext generalPlanContext) {

    List<AdSet> adSets = generalPlanContext.getAdSets();
    ConcurrencyData concurrency = generalPlanContext.getConcurrencyData();
    Languages languages = generalPlanContext.getLanguages();

    List<Request> qualifiedByConcurrency = concurrency.getStreams().stream()
      .map(contentStream -> qualifyByConcurrency(contentStream, adSets, languages))
      .collect(Collectors.toList());


    return generalPlanContext.getBreakDetails().parallelStream()
      .flatMap(breakDetail -> breakDetail.getBreakDuration()
        .parallelStream()
        .map(breakDuration -> buildGraphContextForEachPlan(generalPlanContext, qualifiedByConcurrency, breakDetail,
          breakDuration)))
      .collect(Collectors.toList());

  }

  private GraphContext buildGraphContextForEachPlan(GeneralPlanContext generalPlanContext,
                                                    List<Request> qualifiedByConcurrency, BreakDetail breakDetail,
                                                    Integer breakDuration) {
    List<Request> requests = qualifiedByConcurrency.stream()
      .map(request -> qualifyByBreak(request, breakDetail, breakDuration))
      .collect(Collectors.toList());

    return GraphContext.builder()
      .breakDuration(breakDuration)
      .breakDetail(breakDetail)
      .planType(PlanType.SPOT)
      .requests(requests)
      .responses(generalPlanContext.getResponses())
      .build();
  }


  private Request qualifyByConcurrency(ContentStream contentStream, List<AdSet> adSets, Languages languages) {
    QualificationEngine<AdSet> qualificationEngine = new StreamQualificationEngine(contentStream, languages);
    return Request.builder()
      .concurrency(contentStream.getConcurrency())
      .qualifiedAdSets(qualificationEngine.qualify(adSets))
      .concurrencyId(contentStream.getConcurrencyId())
      .build();
  }

  private Request qualifyByBreak(Request request, BreakDetail breakDetail,
                                 Integer breakDuration) {
    QualificationEngine<QualifiedAdSet> qualificationEngine =
      new StreamBreakQualificationEngine(breakDuration, breakDetail);

    List<QualifiedAdSet> qualifiedAdSets = qualificationEngine.qualify(request.getQualifiedAdSets());
    return Request.builder()
      .concurrencyId(request.getConcurrencyId())
      .concurrency(request.getConcurrency())
      .qualifiedAdSets(qualifiedAdSets)
      .build();
  }
}
