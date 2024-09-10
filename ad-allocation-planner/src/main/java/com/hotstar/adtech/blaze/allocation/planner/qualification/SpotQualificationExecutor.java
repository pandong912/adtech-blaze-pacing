package com.hotstar.adtech.blaze.allocation.planner.qualification;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.QUALIFICATION;
import static com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators.buildSsaiTargetingEvaluators;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.RequestFeasible;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEvaluators;
import com.hotstar.adtech.blaze.allocation.planner.qualification.result.BitSetQualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.qualification.result.QualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.AdSetRemainImpr;
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
    Map<Integer, AdSetRemainImpr> index2AdSet = generalPlanContext.buildRemainDeliveryMap();
    List<ContentStream> streams = generalPlanContext.getConcurrencyData().getStreams();
    Integer breakTypeId = breakTypeIds.getFirst();
    Integer relaxedDuration = generalPlanContext.getRelaxedDuration(breakTypeId, duration);
    TargetingEvaluators targetingEvaluators = buildSsaiTargetingEvaluators(generalPlanContext.getTargetingEvaluators());

    List<RequestFeasible> stream = streams
      .stream()
      .map(contentStream -> streamQualify(contentStream, index2AdSet, relaxedDuration, breakTypeId,
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

  private RequestFeasible streamQualify(ContentStream contentStream, Map<Integer, AdSetRemainImpr> adSets,
                                        int relaxedDuration,
                                        Integer breakTypeId, TargetingEvaluators targetingEvaluators) {
    BitSet qualified = streamQualificationEngine.qualify(contentStream.getPlayoutStream(),
      contentStream.getConcurrency(), adSets, relaxedDuration, breakTypeId, targetingEvaluators);
    return RequestFeasible.builder()
      .bitSet(qualified)
      .supplyId(contentStream.getConcurrencyIdInStream())
      .build();
  }
}
