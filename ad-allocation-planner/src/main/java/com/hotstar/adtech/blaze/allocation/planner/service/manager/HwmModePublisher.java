package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.TaskStatus;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.GeneralPlanContextLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.s3.AllocationDataClient;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Profile("!sim && !worker")
public class HwmModePublisher {

  private final GeneralPlanContextLoader generalPlanContextLoader;
  private final TaskPublisher taskPublisher;
  private final AllocationDataClient allocationDataClient;

  public void publishPlan(Match match, AdModel adModel) {
    try {
      GeneralPlanContext generalPlanContext = generalPlanContextLoader.getGeneralPlanContext(match, adModel);
      if (generalPlanContext.isEmpty()) {
        return;
      }
      Instant version = Instant.now();
      allocationDataClient.uploadHwmData(match.getContentId(), version.toString(), generalPlanContext);
      taskPublisher.publish(match, generalPlanContext, version, this::buildSsaiAllocationPlanResultDetail,
        this::buildSpotAllocationPlanResultDetail);
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
        .algorithmType(AlgorithmType.HWM)
        .breakTypeIds(breakTypeGroup.getBreakTypeIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
        .planType(PlanType.SSAI)
        .duration(duration)
        .taskStatus(TaskStatus.PUBLISHED)
        .build());
  }


}
