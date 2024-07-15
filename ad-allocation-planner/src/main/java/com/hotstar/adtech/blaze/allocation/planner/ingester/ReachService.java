package com.hotstar.adtech.blaze.allocation.planner.ingester;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_REACH_FETCH;

import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocationdata.client.model.DegradationReachStorage;
import com.hotstar.adtech.blaze.allocationdata.client.model.ReachStorage;
import com.hotstar.adtech.blaze.allocationdata.client.model.RedisReachStorage;
import com.hotstar.adtech.blaze.pacing.redis.ReachDataRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReachService {
  private final ReachDataRepository reachDataRepository;
  public static final int SHARD = 50;

  @Timed(MATCH_REACH_FETCH)
  public ReachStorage getUnReachRatio(String contentId, Map<String, Integer> concurrencyIdMap,
                                      Map<Long, Integer> adSetIdToReachIndex) {
    if (adSetIdToReachIndex.isEmpty()) {
      return new DegradationReachStorage();
    }

    int supplySize = concurrencyIdMap.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    double[][] unReachStore = new double[adSetIdToReachIndex.size()][supplySize];

    for (double[] row : unReachStore) {
      Arrays.fill(row, 1.0);
    }

    IntStream
      .range(0, SHARD)
      .parallel()
      .mapToObj(shard -> getReachRatio(contentId, shard))
      .forEach(reach -> fillReachArray(concurrencyIdMap, adSetIdToReachIndex, reach, unReachStore));

    doStatistics(unReachStore, contentId);
    return new RedisReachStorage(unReachStore);
  }

  private Map<String, Map<String, Double>> getReachRatio(String contentId, int shard) {
    try {
      return reachDataRepository.batchGetContentCohortReachRatio(contentId, shard);
    } catch (QueryTimeoutException timeoutException) {
      log.warn("Failed to fetch reach data from redis due to timeout", timeoutException);
      Metrics.counter(MetricNames.REDIS_TIMEOUT_EXCEPTION, "operation", "getUnReach").increment();
      return Collections.emptyMap();
    } catch (Exception e) {
      log.error("Failed to add break cohort creative to redis", e);
      Metrics.counter(MetricNames.REDIS_OTHER_EXCEPTION, "exception", e.getClass().getSimpleName(), "operation",
        "getUnReach").increment();
      return Collections.emptyMap();
    }
  }

  private static void fillReachArray(Map<String, Integer> concurrencyIdMap, Map<Long, Integer> adSetIdToDemandId,
                                     Map<String, Map<String, Double>> unReachResponses, double[][] unReachStore) {
    for (Map.Entry<String, Map<String, Double>> unReachResponse : unReachResponses.entrySet()) {
      Integer supplyId = concurrencyIdMap.get(unReachResponse.getKey());
      if (supplyId == null) {
        continue;
      }
      for (Map.Entry<String, Double> unReachData : unReachResponse.getValue().entrySet()) {
        Integer demandId = adSetIdToDemandId.get(Long.parseLong(unReachData.getKey()));
        if (demandId == null) {
          continue;
        }
        unReachStore[demandId][supplyId] = unReachData.getValue();
      }
    }
  }

  private void doStatistics(double[][] unReachStore, String contentId) {
    log.info("{}: unReachStore size is {} * {}", contentId, unReachStore.length, unReachStore[0].length);
    int totalCount = 0;
    for (int supply = 0; supply < unReachStore[0].length; supply++) {
      for (double[] doubles : unReachStore) {
        if (doubles[supply] < 1.0) {
          totalCount++;
        }
      }
    }
    log.info("{}: percentage of unReachRatio smaller than 1 is {}", contentId,
      (double) totalCount / (unReachStore.length * unReachStore[0].length));
  }
}
