package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_WORKER_GENERATE_PLAN;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.service.AllocationPlanTaskService;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.ShalePlanContext;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
@Profile("!sim && !master")
public class AllocationPlanWorker {

  private final AllocationPlanTaskService allocationPlanTaskService;
  private final AllocationDataLoader allocationDataLoader;
  private final ShalePlanWorker shalePlanWorker;
  private final HwmPlanWorker hwmPlanWorker;
  private final AllocationPlanResultService resultService;

  @Scheduled(fixedRateString = "${blaze.ad-allocation-planner.schedule.worker:500}", initialDelayString = "1000")
  @Timed(value = MATCH_WORKER_GENERATE_PLAN, histogram = true)
  public void generatePlan() {
    try {
      allocationPlanTaskService.takeOneSubTask().ifPresent(this::buildPlan);
    } catch (CannotAcquireLockException e) {
      log.info("Failed to acquire lock for allocation plan result detail table");
    }
  }

  public void buildPlan(AllocationPlanResultDetail subtask) {
    AllocationPlanResult task = allocationPlanTaskService.getTask(subtask);
    try {
      if (task.getVersion().plus(4, ChronoUnit.MINUTES).isBefore(Instant.now())) {
        allocationPlanTaskService.subTaskExpired(subtask);
        log.error("subtask expired: {}, parent task detail: {}", subtask, task);
        return;
      }
      UploadResult uploadResult = subtask.getAlgorithmType() == AlgorithmType.SHALE
        ? generateShalePlan(subtask, task) : generateHwmPlan(subtask, task);
      allocationPlanTaskService.subTaskSuccess(subtask, uploadResult);
    } catch (Exception e) {
      log.error("subtask failed: {}, parent task detail: {}", subtask, task, e);
      allocationPlanTaskService.subTaskFail(subtask);
    }
  }

  private UploadResult generateHwmPlan(AllocationPlanResultDetail subtask, AllocationPlanResult task) {
    GeneralPlanContext generalPlanContext =
      allocationDataLoader.loadGeneralData(task.getContentId(), task.getVersion());
    HwmAllocationPlan hwmAllocationPlan =
      hwmPlanWorker.generatePlans(generalPlanContext, subtask.getPlanType(),
        parseBreakTypeIds(subtask.getBreakTypeIds()), subtask.getDuration());
    return resultService.uploadHwmPlan(task, hwmAllocationPlan);

  }

  private UploadResult generateShalePlan(AllocationPlanResultDetail subtask, AllocationPlanResult task) {
    ShalePlanContext shalePlanContext =
      allocationDataLoader.loadShaleData(task.getContentId(), task.getVersion());
    ShaleAllocationPlan shaleAllocationPlan =
      shalePlanWorker.generatePlans(shalePlanContext, subtask.getPlanType(),
        parseBreakTypeIds(subtask.getBreakTypeIds()), subtask.getDuration());
    return resultService.uploadShalePlan(task, shaleAllocationPlan);
  }

  private List<Integer> parseBreakTypeIds(String breakTypeIds) {
    return Arrays.stream(breakTypeIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
  }

}
