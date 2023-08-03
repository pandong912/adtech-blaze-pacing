package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.DIAGNOSIS;

import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Platform;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.AllocationDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.CohortConcurrencyDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ConcurrencyDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.HwmAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ShaleAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.StreamConcurrencyDiagnosis;
import com.hotstar.adtech.blaze.allocationplan.client.AllocationPlanClient;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationDiagnosisService {

  private final AllocationPlanClient allocationPlanClient;

  @Timed(DIAGNOSIS)
  public void uploadAllocationDiagnosis(String contentId, Instant version,
                                        List<HwmAllocationDiagnosisDetail> hwmAllocationDiagnosisDetails,
                                        List<ShaleAllocationDiagnosisDetail> shaleAllocationDiagnosisDetails,
                                        ConcurrencyData concurrencyContext) {
    AllocationDiagnosis allocationDiagnosis = AllocationDiagnosis.builder()
      .contentId(contentId)
      .version(version)
      .hwmAllocationDiagnosisDetails(hwmAllocationDiagnosisDetails)
      .shaleAllocationDiagnosisDetails(shaleAllocationDiagnosisDetails)
      .concurrencyDiagnosis(getConcurrencyDiagnosis(concurrencyContext))
      .build();
    allocationPlanClient.uploadAllocationDiagnosis(allocationDiagnosis);
  }

  private ConcurrencyDiagnosis getConcurrencyDiagnosis(ConcurrencyData concurrencyContext) {
    return ConcurrencyDiagnosis.builder()
      .cohorts(concurrencyContext.getCohorts().stream()
        .map(this::buildCohortConcurrencyDiagnosis)
        .collect(Collectors.toList()))
      .streams(concurrencyContext.getStreams().stream()
        .map(this::buildStreamConcurrencyDiagnosis).collect(Collectors.toList()))
      .build();
  }

  private StreamConcurrencyDiagnosis buildStreamConcurrencyDiagnosis(ContentStream contentStream) {
    return StreamConcurrencyDiagnosis.builder()
      .concurrency(contentStream.getConcurrency())
      .tenant(contentStream.getPlayoutStream().getTenant())
      .streamType(contentStream.getPlayoutStream().getStreamType())
      .cohortId(contentStream.getConcurrencyId())
      .contentId(contentStream.getContentId())
      .language(contentStream.getPlayoutStream().getLanguage().getName())
      .platforms(
        contentStream.getPlayoutStream().getPlatforms().stream().map(Platform::getName)
          .collect(Collectors.toList()))
      .build();
  }

  private CohortConcurrencyDiagnosis buildCohortConcurrencyDiagnosis(ContentCohort contentCohort) {
    return CohortConcurrencyDiagnosis.builder()
      .concurrency(contentCohort.getConcurrency())
      .tenant(contentCohort.getPlayoutStream().getTenant())
      .cohortId(contentCohort.getConcurrencyId())
      .contentId(contentCohort.getContentId())
      .streamType(contentCohort.getStreamType())
      .language(contentCohort.getPlayoutStream().getLanguage().getName())
      .platforms(
        contentCohort.getPlayoutStream().getPlatforms().stream().map(Platform::getName)
          .collect(Collectors.toList()))
      .ssaiTag(contentCohort.getSsaiTag())
      .build();
  }
}
