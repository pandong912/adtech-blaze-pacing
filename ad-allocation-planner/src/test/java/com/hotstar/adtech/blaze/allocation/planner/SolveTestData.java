package com.hotstar.adtech.blaze.allocation.planner;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.qualification.result.ArrayQualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.qualification.result.QualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.Request;
import com.hotstar.adtech.blaze.allocationdata.client.model.Response;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SolveTestData {
  public static GraphContext getGraphContext(Random random, int cohortSize, int adSetSize, int maxConcurrency,
                                             int edge) {

    List<Request> requests = IntStream.range(0, cohortSize)
      .mapToObj(id -> Request.builder()
        .concurrencyId(id)
        .streamType(StreamType.SSAI_Spot)
        .concurrency(random.nextInt(maxConcurrency))
        .build())
      .collect(Collectors.toList());
    List<Response> responses = IntStream.range(0, adSetSize).mapToObj(
      adSetId -> Response.builder()
        .adSetId(adSetId)
        .demandId(adSetId)
        .order(random.nextInt(3))
        .demand(random.nextDouble() * maxConcurrency / 2)
        .adDuration(10000)
        .maximizeReach(1)
        .reachIndex(adSetId)
        .build()
    ).collect(Collectors.toList());

    QualificationResult bitSet = new ArrayQualificationResult(requests.size(), adSetSize);
    for (Request request : requests) {
      for (int i = 0; i < edge; i++) {
        bitSet.set(request.getConcurrencyId(), i);
      }
    }


    return GraphContext.builder()
      .breakDuration(20000)
      .requests(requests)
      .responses(responses)
      .edges(bitSet)
      .planType(PlanType.SSAI)
      .build();
  }
}
