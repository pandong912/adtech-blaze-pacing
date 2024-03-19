package com.hotstar.adtech.blaze.allocation.planner.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;
import static com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators.buildSsaiTargetingEvaluators;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import io.micrometer.core.annotation.Timed;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpotQualificationExecutor {
  private final StreamQualificationEngine streamQualificationEngine;

  @Timed(value = QUALIFICATION, extraTags = {"type", "spot"})
  public GraphContext executeQualify(GeneralPlanContext generalPlanContext,
                                     List<Integer> breakTypeIds, Integer duration) {
    List<AdSet> adSets = generalPlanContext.getAdSets();
    Map<Integer, AdSet> index2AdSet = adSets.stream().collect(Collectors.toMap(AdSet::getDemandId, adSet -> adSet));
    List<ContentStream> streams = generalPlanContext.getConcurrencyData().getStreams();
    Integer relaxedDuration = generalPlanContext.getRelaxedDuration(breakTypeIds.get(0), duration);
    TargetingEvaluators targetingEvaluators = buildSsaiTargetingEvaluators(generalPlanContext.getTargetingEvaluators());

    List<RequestFeasible> stream = streams
      .stream()
      .map(contentStream -> streamQualify(contentStream, index2AdSet, relaxedDuration, breakTypeIds.get(0),
        targetingEvaluators))
      .collect(Collectors.toList());

    QualificationResult result =
      new BitSetQualificationResult(streams.size(), targetingEvaluators.getAdSetSize(), stream);


    return GraphContext.builder()
      .breakDuration(duration)
      .planType(PlanType.SPOT)
      .requests(generalPlanContext.getRequestData().getSpotRequests())
      .edges(result)
      .responses(generalPlanContext.getResponses())
      .breakTypeIds(breakTypeIds)
      .build();

  }

  private RequestFeasible streamQualify(ContentStream contentStream, Map<Integer, AdSet> adSets, int relaxedDuration,
                                        Integer breakTypeId, TargetingEvaluators targetingEvaluators) {
    BitSet qualified = streamQualificationEngine.qualify(contentStream.getPlayoutStream(), adSets,
      relaxedDuration, breakTypeId, targetingEvaluators);
    return RequestFeasible.builder()
      .bitSet(qualified)
      .supplyId(contentStream.getConcurrencyIdInStream())
      .build();
  }
}
