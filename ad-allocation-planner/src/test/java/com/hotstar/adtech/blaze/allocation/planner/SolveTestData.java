package com.hotstar.adtech.blaze.allocation.planner;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Request;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class SolveTestData {
  public static GraphContext getGraphContext(Random random, int cohortSize, int adSetSize, int maxConcurrency) {

    List<Request> requests = IntStream.range(0, cohortSize)
      .mapToObj(id -> Request.builder()
        .concurrencyId(id)
        .concurrency(random.nextInt(maxConcurrency))
        .qualifiedAdSets(LongStream.range(0, adSetSize)
          .mapToObj(e -> QualifiedAdSet.builder()
            .id(e)
            .qualifiedAds(Collections.emptyList())
            .build())
          .collect(Collectors.toList()))
        .build())
      .collect(Collectors.toList());
    List<Response> responses = IntStream.range(0, adSetSize).mapToObj(
      adSetId -> Response.builder()
        .adSetId(adSetId)
        .demandId(adSetId + 10)
        .order(random.nextInt(3))
        .demand(random.nextDouble() * maxConcurrency / 2)
        .adDuration(10000)
        .maximizeReach(1)
        .build()
    ).collect(Collectors.toList());


    return GraphContext.builder()
      .breakDuration(20000)
      .breakTypeGroup(null)
      .requests(requests)
      .responses(responses)
      .planType(PlanType.SSAI)
      .build();
  }
}
