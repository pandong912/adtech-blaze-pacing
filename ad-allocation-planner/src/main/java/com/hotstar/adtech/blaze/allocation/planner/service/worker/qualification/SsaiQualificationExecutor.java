package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortAdQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.CohortAdSetQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamAdQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamAdSetQualificationEngine;
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
  public GraphContext executeQualify(GeneralPlanContext generalPlanContext,
                                     List<Integer> breakTypeIds, Integer duration) {
    List<AdSet> adSets = generalPlanContext.getAdSets();
    ConcurrencyData concurrency = generalPlanContext.getConcurrencyData();
    List<ContentCohort> mixedStreamCohorts = concurrency.getCohorts();
    List<ContentStream> streams = concurrency.getStreams().stream()
      .filter(stream -> stream.getPlayoutStream().getStreamType() == StreamType.Spot)
      .collect(Collectors.toList());

    RequestData requestData = generalPlanContext.getRequestData();
    QualificationResult firstQualified =
      new BitSetQualificationResult(requestData.getSsaiAndSpotRequests().size(), adSets.size());
    mixedStreamCohorts
      .parallelStream()
      .forEach(contentCohort -> qualifyByConcurrency(contentCohort, adSets,
        generalPlanContext.getAttributeId2TargetingTagMap(), firstQualified));
    streams
      .parallelStream()
      .forEach(contentStream -> qualifyByConcurrency(contentStream, adSets, firstQualified));

    QualificationResult secondQualified =
      new BitSetQualificationResult(requestData.getSsaiAndSpotRequests().size(), adSets.size());
    mixedStreamCohorts
      .parallelStream()
      .forEach(request -> qualifyByBreak(request, adSets, firstQualified, secondQualified, duration));
    streams
      .parallelStream()
      .forEach(
        request -> qualifyByBreak(adSets, request, breakTypeIds.get(0), duration, firstQualified, secondQualified));

    return GraphContext.builder()
      .breakDuration(duration)
      .planType(PlanType.SSAI)
      .requests(requestData.getSsaiAndSpotRequests())
      .edges(secondQualified)
      .responses(generalPlanContext.getResponses())
      .breakTypeIds(breakTypeIds)
      .build();
  }


  private void qualifyByConcurrency(ContentCohort contentCohort, List<AdSet> adSets,
                                    Map<String, Integer> attributeId2TargetingTagMap,
                                    QualificationResult firstQualified) {

    CohortAdSetQualificationEngine qualificationEngine =
      new CohortAdSetQualificationEngine(contentCohort.getSsaiTag(), attributeId2TargetingTagMap,
        contentCohort.getConcurrencyId(), firstQualified);

    qualificationEngine.qualify(adSets);
  }

  private void qualifyByConcurrency(ContentStream contentStream, List<AdSet> adSets,
                                    QualificationResult firstQualified) {
    QualificationEngine qualificationEngine =
      new StreamAdSetQualificationEngine(contentStream.getPlayoutStream(), contentStream.getConcurrencyIdInCohort(),
        firstQualified);
    qualificationEngine.qualify(adSets);
  }

  private void qualifyByBreak(ContentCohort request, List<AdSet> adSets, QualificationResult firstQualified,
                              QualificationResult secondQualified,
                              Integer breakDuration) {
    Integer languageId = request.getPlayoutStream().getLanguage().getId();
    CohortAdQualificationEngine qualificationEngine =
      new CohortAdQualificationEngine(breakDuration, languageId, request.getConcurrencyId(), firstQualified,
        secondQualified);

    qualificationEngine.qualify(adSets);
  }

  private void qualifyByBreak(List<AdSet> adSets, ContentStream request, Integer breakTypeId, Integer breakDuration,
                              QualificationResult firstQualified, QualificationResult secondQualified) {
    Language language = request.getPlayoutStream().getLanguage();
    QualificationEngine qualificationEngine =
      new StreamAdQualificationEngine(breakDuration, breakTypeId, language, request.getConcurrencyIdInCohort(),
        firstQualified, secondQualified);

    qualificationEngine.qualify(adSets);
  }
}
