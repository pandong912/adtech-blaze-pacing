package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import static com.hotstar.adtech.blaze.allocation.diagnosis.metric.MetricNames.ALLOCATION_CONCURRENCY;

import com.hotstar.adtech.blaze.admodel.common.enums.Ladder;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationCohortConcurrency;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AllocationConcurrencySink;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocationdata.client.AllocationDataClient;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationConcurrencyService {
  private final AllocationDataClient allocationDataClient;
  private final AllocationConcurrencySink allocationConcurrencySink;

  @Timed(ALLOCATION_CONCURRENCY)
  public void writeConcurrency(AllocationPlanResult result) {
    String[] split = result.getPath().split("/");
    String contentId = split[1];
    String versionString = split[2];
    GeneralPlanContext generalPlanContext = allocationDataClient.loadHwmData(contentId, versionString);
    Stream<AllocationCohortConcurrency> streams = generalPlanContext.getConcurrencyData().getStreams().stream()
      .map(stream -> buildStreamConcurrency(stream, contentId, result));

    Stream<AllocationCohortConcurrency> cohorts = generalPlanContext.getConcurrencyData().getCohorts().stream()
      .map(cohort -> buildCohortConcurrency(cohort, contentId, result));

    List<AllocationCohortConcurrency> collect = Stream.concat(streams, cohorts).collect(Collectors.toList());
    allocationConcurrencySink.write(collect);

    log.info("write concurrency data to clickhouse, contentId: {}, version: {}, concurrency: {}",
      contentId, versionString, collect.size());
  }

  private AllocationCohortConcurrency buildCohortConcurrency(ContentCohort cohort, String contentId,
                                                             AllocationPlanResult result) {
    return AllocationCohortConcurrency.builder()
      .concurrency(cohort.getConcurrency())
      .contentId(contentId)
      .siMatchId(contentId)
      .version(result.getVersion())
      .cohortId(cohort.getConcurrencyId())
      .language(cohort.getPlayoutStream().getLanguage().getName())
      .platforms(
        cohort.getPlayoutStream().getLadders().stream().map(Ladder::toString).collect(Collectors.joining("+")))
      .tenant(cohort.getPlayoutStream().getTenant().getName())
      .ssaiTag(cohort.getSsaiTag())
      .streamType(cohort.getPlayoutStream().getStreamType().name())
      .playoutId(cohort.getPlayoutStream().getPlayoutId())
      .build();
  }

  private AllocationCohortConcurrency buildStreamConcurrency(ContentStream stream, String contentId,
                                                             AllocationPlanResult result) {
    return AllocationCohortConcurrency.builder()
      .concurrency(stream.getConcurrency())
      .contentId(contentId)
      .siMatchId(contentId)
      .version(result.getVersion())
      .cohortId(stream.getConcurrencyIdInCohort())
      .language(stream.getPlayoutStream().getLanguage().getName())
      .platforms(
        stream.getPlayoutStream().getLadders().stream().map(Ladder::toString).collect(Collectors.joining("+")))
      .tenant(stream.getPlayoutStream().getTenant().getName())
      .ssaiTag("")
      .streamType(stream.getPlayoutStream().getStreamType().name())
      .playoutId(stream.getPlayoutStream().getPlayoutId())
      .build();
  }
}
