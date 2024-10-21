package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.BUILD_FAILING;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_PLAN_UPDATE;

import com.hotstar.adtech.blaze.admodel.common.enums.TaskStatus;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import com.hotstar.adtech.blaze.allocation.planner.launchdarkly.DynamicConfig;
import com.hotstar.adtech.blaze.allocation.planner.service.AllocationPlanTaskService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
@Profile("!sim && !worker")
public class AllocationPlanManager {

  private final HwmModePublisher hwmModePublisher;
  private final ShaleModePublisher shaleModePublisher;
  private final ShaleAndHwmModePublisher shaleAndHwmModePublisher;
  private final DynamicConfig dynamicConfig;
  private final DataLoader dataLoader;
  private final AllocationPlanTaskService allocationPlanTaskService;

  @Value("${blaze.ad-allocation-planner.gap-time:30000}")
  private long planGapTime;

  @Scheduled(fixedRateString = "${blaze.ad-allocation-planner.schedule.manager:3000}", initialDelayString = "1000")
  @Timed(value = MATCH_PLAN_UPDATE, histogram = true)
  public void generatePlan() {
    AdModel adModel = dataLoader.getAdModel();
    adModel.getMatches().values().parallelStream()
      .forEach(match -> generateAndUploadAllocationPlan(match, adModel));
  }

  private void generateAndUploadAllocationPlan(Match match, AdModel adModel) {
    try {
      AllocationPlanResult latestTask = allocationPlanTaskService.getLatestTask(match.getContentId());
      checkTaskStatus(latestTask);
      if (shouldPublishNew(latestTask)) {
        publishNewTask(match, adModel);
      }
    } catch (Exception e) {
      log.error("fail to build plan for content:" + match.getContentId(), e);
      Metrics.counter(BUILD_FAILING, "contentId", match.getContentId()).increment();
    }
  }

  private void checkTaskStatus(AllocationPlanResult latestTask) {
    if (latestTask == null) {
      return;
    }

    List<AllocationPlanResultDetail> details = allocationPlanTaskService.getSubTaskList(latestTask.getId());
    boolean pending = details.stream().anyMatch(detail -> detail.getTaskStatus().isPending());
    boolean success = details.stream().allMatch(detail -> detail.getTaskStatus().isSuccess());

    if (pending) {
      if (latestTask.getVersion().plus(8, ChronoUnit.MINUTES).isBefore(Instant.now())) {
        log.error("task is pending for more than 8 minutes, task:{}, subTask size: {}", latestTask.getVersion(),
          details.size());
        allocationPlanTaskService.expirePendingSubTask(details);
        allocationPlanTaskService.taskExpired(latestTask);
      }
    } else {
      if (success) {
        allocationPlanTaskService.taskSuccess(latestTask);
      } else {
        allocationPlanTaskService.taskFailed(latestTask);
        log.error("task is failed, task:{}, subTask size: {}", latestTask, details.size());
      }
    }
  }

  private boolean shouldPublishNew(AllocationPlanResult latestTask) {
    if (latestTask == null) {
      return true;
    }
    if (latestTask.getVersion().plus(planGapTime, ChronoUnit.SECONDS).isAfter(Instant.now())) {
      return false;
    }
    return latestTask.getTaskStatus() != TaskStatus.PUBLISHED;
  }

  private void publishNewTask(Match match, AdModel adModel) {
    AllocationPlanMode allocationPlanMode = dynamicConfig.getAllocationPlanMode();
    switch (allocationPlanMode) {
      case HWM:
        hwmModePublisher.publishPlan(match, adModel);
        break;
      case SHALE:
        shaleModePublisher.publishPlan(match, adModel);
        break;
      case SHALE_HWM:
        shaleAndHwmModePublisher.publishPlan(match, adModel);
        break;
      default:
        throw new IllegalArgumentException("unknown allocation plan mode:" + allocationPlanMode);
    }
  }
}
