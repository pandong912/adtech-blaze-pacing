package com.hotstar.adtech.blaze.allocation.planner;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Request;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SolveTestData {
  public static GraphContext getGraphContext(Random random, int cohortSize, int adSetSize, int maxConcurrency) {

    List<Request> requests = IntStream.range(0, cohortSize)
      .mapToObj(id -> Request.builder()
        .concurrencyId(id)
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
        .build()
    ).collect(Collectors.toList());

    BitSet bitSet = new BitSet();
    int size = MemoryAlignment.getSize(responses);
    for (Request request : requests) {
      for (int i = 0; i < 2000; i++) {
        bitSet.set(request.getConcurrencyId() * size + i);
      }
    }


    return GraphContext.builder()
      .breakDuration(20000)
      .breakTypeGroup(null)
      .requests(requests)
      .responses(responses)
      .edges(bitSet)
      .planType(PlanType.SSAI)
      .build();
  }
}
