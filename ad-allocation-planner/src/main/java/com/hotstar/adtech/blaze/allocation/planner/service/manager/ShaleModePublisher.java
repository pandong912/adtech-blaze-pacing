package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.TaskStatus;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.ShalePlanContextLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.s3.AllocationDataClient;
import com.hotstar.adtech.blaze.allocationplan.client.AllocationPlanClient;
import com.hotstar.adtech.blaze.allocationplan.client.common.PathUtils;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
@Profile("!sim && !worker")
public class ShaleModePublisher {

  private final ShalePlanContextLoader shalePlanContextLoader;
  private final TaskPublisher taskPublisher;
  private final AllocationDataClient allocationDataClient;
  private final AllocationPlanClient allocationPlanClient;

  @Timed(value = MetricNames.GENERATOR, extraTags = {"type", "shale"})
  public void publishPlan(Match match, AdModel adModel) {
    try {
      Pair<Map<String, Integer>, ShalePlanContext> result =
        shalePlanContextLoader.getShalePlanContext(match, adModel);
      ShalePlanContext shalePlanContext = result.getRight();
      Map<String, Integer> supplyIdMap = result.getLeft();
      GeneralPlanContext generalPlanContext = shalePlanContext.getGeneralPlanContext();
      if (generalPlanContext.isEmpty()) {
        return;
      }
      Instant version = Instant.now();
      String path = PathUtils.joinToPath(match.getContentId(), version);
      allocationPlanClient.uploadSupplyIdMap(path, supplyIdMap);
      allocationDataClient.uploadShaleData(match.getContentId(), version.toString(), shalePlanContext);
      allocationDataClient.uploadHwmData(match.getContentId(), version.toString(),
        shalePlanContext.getGeneralPlanContext());

      taskPublisher.publish(match, generalPlanContext, version, this::buildSpotAllocationPlanResultDetail,
        Collections.singletonList(this::buildSsaiAllocationPlanResultDetail));
    } catch (Exception e) {
      throw new ServiceException("Failed to publish task for match: " + match.getContentId(), e);
    }
  }

  private Stream<AllocationPlanResultDetail> buildSpotAllocationPlanResultDetail(BreakTypeGroup breakTypeGroup) {
    return breakTypeGroup.getAllBreakDurations().stream().map(duration ->
      AllocationPlanResultDetail.builder()
        .algorithmType(AlgorithmType.HWM)
        .breakTypeIds(breakTypeGroup.getBreakTypeIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
        .planType(PlanType.SPOT)
        .duration(duration)
        .taskStatus(TaskStatus.PUBLISHED)
        .build());
  }


  private Stream<AllocationPlanResultDetail> buildSsaiAllocationPlanResultDetail(BreakTypeGroup breakTypeGroup) {
    return breakTypeGroup.getAllBreakDurations().stream().map(duration ->
      AllocationPlanResultDetail.builder()
        .algorithmType(AlgorithmType.SHALE)
        .breakTypeIds(breakTypeGroup.getBreakTypeIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
        .planType(PlanType.SSAI)
        .duration(duration)
        .taskStatus(TaskStatus.PUBLISHED)
        .build());
  }

}
