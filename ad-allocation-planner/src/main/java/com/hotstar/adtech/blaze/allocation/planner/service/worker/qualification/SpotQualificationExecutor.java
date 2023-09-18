package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamAdQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamAdSetQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SpotQualificationExecutor {


  @Timed(value = QUALIFICATION, extraTags = {"type", "spot"})
  public GraphContext executeQualify(GeneralPlanContext generalPlanContext,
                                     List<Integer> breakTypeIds, Integer duration) {

    List<AdSet> adSets = generalPlanContext.getAdSets();
    List<ContentStream> streams = generalPlanContext.getConcurrencyData().getStreams();
    Integer relaxedDuration = generalPlanContext.getRelaxedDuration(breakTypeIds.get(0), duration);

    QualificationResult firstQualified = new BitSetQualificationResult(streams.size(), adSets.size());

    streams.forEach(contentStream -> qualifyByConcurrency(contentStream, adSets, firstQualified));

    QualificationResult secondQualified = new BitSetQualificationResult(streams.size(), adSets.size());
    generalPlanContext.getConcurrencyData().getStreams()
      .forEach(request -> qualifyByBreak(generalPlanContext.getAdSets(), request, breakTypeIds.get(0), relaxedDuration,
        firstQualified, secondQualified));


    return GraphContext.builder()
      .breakDuration(duration)
      .planType(PlanType.SPOT)
      .requests(generalPlanContext.getRequestData().getSpotRequests())
      .edges(secondQualified)
      .responses(generalPlanContext.getResponses())
      .breakTypeIds(breakTypeIds)
      .build();

  }

  private void qualifyByConcurrency(ContentStream contentStream, List<AdSet> adSets,
                                    QualificationResult firstQualified) {
    QualificationEngine qualificationEngine =
      new StreamAdSetQualificationEngine(contentStream.getPlayoutStream(), contentStream.getConcurrencyIdInStream(),
        firstQualified);
    qualificationEngine.qualify(adSets);
  }

  private void qualifyByBreak(List<AdSet> adSets, ContentStream request, Integer breakTypeId, Integer relaxedDuration,
                              QualificationResult firstQualified, QualificationResult secondQualified) {
    Language language = request.getPlayoutStream().getLanguage();
    QualificationEngine qualificationEngine =
      new StreamAdQualificationEngine(relaxedDuration, breakTypeId, language, request.getConcurrencyIdInStream(),
        firstQualified, secondQualified);

    qualificationEngine.qualify(adSets);
  }
}
