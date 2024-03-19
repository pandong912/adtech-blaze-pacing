package com.hotstar.adtech.blaze.allocation.planner.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;
import static com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators.buildSsaiTargetingEvaluators;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.RequestData;
import io.micrometer.core.annotation.Timed;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SsaiQualificationExecutor {
  public static final String SSAI_TAG_PREFIX = "SSAI::";
  private final CohortQualificationEngine cohortQualificationEngine;
  private final StreamQualificationEngine streamQualificationEngine;

  @Timed(value = QUALIFICATION, extraTags = {"type", "ssai"})
  public GraphContext executeQualify(GeneralPlanContext generalPlanContext,
                                     List<Integer> breakTypeIds, Integer duration) {
    List<AdSet> adSets = generalPlanContext.getAdSets();
    Map<Integer, AdSet> index2AdSet = adSets.stream().collect(Collectors.toMap(AdSet::getDemandId, adSet -> adSet));
    ConcurrencyData concurrency = generalPlanContext.getConcurrencyData();
    List<ContentCohort> mixedStreamCohorts = concurrency.getCohorts();
    List<ContentStream> streams = concurrency.getStreams().stream()
      .filter(stream -> stream.getPlayoutStream().getStreamType() == StreamType.Spot)
      .collect(Collectors.toList());
    RequestData requestData = generalPlanContext.getRequestData();
    TargetingEvaluators targetingEvaluators = buildSsaiTargetingEvaluators(generalPlanContext.getTargetingEvaluators());

    Map<String, Integer> attributeId2TargetingTagMap = generalPlanContext.getAttributeId2TargetingTagMap();
    Integer relaxedDuration = generalPlanContext.getRelaxedDuration(breakTypeIds.get(0), duration);
    Stream<RequestFeasible> cohort = mixedStreamCohorts
      .parallelStream()
      .map(contentCohort -> cohortQualify(contentCohort, index2AdSet, attributeId2TargetingTagMap,
        targetingEvaluators, relaxedDuration));
    Stream<RequestFeasible> stream = streams
      .parallelStream()
      .map(contentStream -> streamQualify(contentStream, index2AdSet, relaxedDuration, breakTypeIds.get(0),
        targetingEvaluators));


    QualificationResult result =
      new BitSetQualificationResult(requestData.getSsaiAndSpotRequests().size(), targetingEvaluators.getAdSetSize(),
        Stream.concat(cohort, stream).collect(Collectors.toList()));

    return GraphContext.builder()
      .breakDuration(duration)
      .planType(PlanType.SSAI)
      .requests(requestData.getSsaiAndSpotRequests())
      .edges(result)
      .responses(generalPlanContext.getResponses())
      .breakTypeIds(breakTypeIds)
      .build();
  }


  private RequestFeasible cohortQualify(ContentCohort contentCohort, Map<Integer, AdSet> adSets,
                                        Map<String, Integer> attributeId2TargetingTagMap,
                                        TargetingEvaluators targetingEvaluators, int duration) {
    Map<Integer, Set<String>> parseSsaiTag = parseSsaiTag(contentCohort.getSsaiTag(),
      attributeId2TargetingTagMap);
    BitSet qualified =
      cohortQualificationEngine.qualify(contentCohort.getPlayoutStream(), adSets, duration, parseSsaiTag,
        targetingEvaluators);
    return RequestFeasible.builder()
      .bitSet(qualified)
      .supplyId(contentCohort.getConcurrencyId())
      .build();
  }

  private RequestFeasible streamQualify(ContentStream contentStream, Map<Integer, AdSet> adSets, int relaxedDuration,
                                        Integer breakTypeId, TargetingEvaluators targetingEvaluators) {
    BitSet qualified = streamQualificationEngine.qualify(contentStream.getPlayoutStream(), adSets,
      relaxedDuration, breakTypeId, targetingEvaluators);
    return RequestFeasible.builder()
      .bitSet(qualified)
      .supplyId(contentStream.getConcurrencyIdInCohort())
      .build();
  }

  private Map<Integer, Set<String>> parseSsaiTag(String ssaiTag,
                                                 Map<String, Integer> targetingTagToAttributeId) {
    Map<Integer, Set<String>> attributeId2TargetingTags = new HashMap<>();
    String[] targetingTags = ssaiTag.substring(SSAI_TAG_PREFIX.length()).split(":");
    for (String targetingTag : targetingTags) {
      Integer attributeId = targetingTagToAttributeId.get(targetingTag);
      if (attributeId == null) {
        continue; // ignore unrecognized tags
      }
      attributeId2TargetingTags.computeIfAbsent(attributeId, id -> new HashSet<>()).add(targetingTag);
    }
    return attributeId2TargetingTags;
  }
}
