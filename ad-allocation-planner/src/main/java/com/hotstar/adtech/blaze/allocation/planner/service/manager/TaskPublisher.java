package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MANAGER_PUBLISH;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.enums.TaskStatus;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroupFactory;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.s3.AllocationDataClient;
import com.hotstar.adtech.blaze.allocationplan.client.AllocationPlanClient;
import com.hotstar.adtech.blaze.allocationplan.client.common.PathUtils;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskPublisher {
  private final AllocationPlanResultRepository allocationPlanResultRepository;
  private final BreakTypeGroupFactory breakTypeGroupFactory;
  private final AllocationDataClient allocationDataClient;
  private final AllocationPlanClient allocationPlanClient;

  @Timed(value = MANAGER_PUBLISH)
  public void uploadAndPublish(Match match, GeneralPlanContext generalPlanContext, Instant version,
                               AlgorithmType spotAlgorithm, AlgorithmType... ssaiAlgorithms) {
    if (generalPlanContext.isEmpty()) {
      return;
    }
    allocationDataClient.uploadHwmData(match.getContentId(), version.toString(), generalPlanContext);
    publish(match, generalPlanContext, version, spotAlgorithm, ssaiAlgorithms);
  }

  @Timed(value = MANAGER_PUBLISH)
  public void uploadAndPublish(Match match, ShalePlanContext shalePlanContext, Map<String, Integer> supplyIdMap,
                               Instant version,
                               AlgorithmType spotAlgorithm, AlgorithmType... ssaiAlgorithms) {
    GeneralPlanContext generalPlanContext = shalePlanContext.getGeneralPlanContext();
    if (generalPlanContext.isEmpty()) {
      return;
    }
    String path = PathUtils.joinToPath(match.getContentId(), version);
    allocationDataClient.uploadHwmData(match.getContentId(), version.toString(), generalPlanContext);
    allocationPlanClient.uploadSupplyIdMap(path, supplyIdMap);
    allocationDataClient.uploadShaleData(match.getContentId(), version.toString(), shalePlanContext);
    publish(match, generalPlanContext, version, spotAlgorithm, ssaiAlgorithms);
  }


  private void publish(Match match, GeneralPlanContext generalPlanContext, Instant version,
                       AlgorithmType spotAlgorithm, AlgorithmType... ssaiAlgorithms) {
    if (generalPlanContext.isEmpty()) {
      return;
    }
    AllocationPlanResult task = AllocationPlanResult.builder()
      .contentId(match.getContentId())
      .version(version)
      .path(PathUtils.joinToPath(match.getContentId(), version))
      .allocationPlanResultDetails(new ArrayList<>())
      .taskStatus(TaskStatus.PUBLISHED)
      .build();
    List<BreakTypeGroup> breakTypeList = generalPlanContext.getBreakTypeList();

    BreakContext breakContext = generalPlanContext.getBreakContext();
    List<AllocationPlanResultDetail> ssaiAllocationPlanResultDetails = Arrays.stream(ssaiAlgorithms)
      .flatMap(ssaiAlgorithm -> breakTypeList.stream()
        .flatMap(breakTypeGroup -> buildAllocationPlanResultDetail(breakTypeGroup, task, breakContext, ssaiAlgorithm,
          PlanType.SSAI)))
      .collect(Collectors.toList());
    List<AllocationPlanResultDetail> spotAllocationPlanResultDetails = breakTypeList.stream()
      .flatMap(breakTypeGroup -> buildAllocationPlanResultDetail(breakTypeGroup, task, breakContext, spotAlgorithm,
        PlanType.SPOT))
      .collect(Collectors.toList());

    task.getAllocationPlanResultDetails().addAll(ssaiAllocationPlanResultDetails);
    task.getAllocationPlanResultDetails().addAll(spotAllocationPlanResultDetails);
    allocationPlanResultRepository.save(task);
  }

  private Stream<AllocationPlanResultDetail> buildAllocationPlanResultDetail(BreakTypeGroup breakTypeGroup,
                                                                             AllocationPlanResult allocationPlanResult,
                                                                             BreakContext breakContext,
                                                                             AlgorithmType algorithmType,
                                                                             PlanType planType) {
    return breakTypeGroup.getAllBreakDurations().stream()
      .map(duration -> AllocationPlanResultDetail.builder()
        .algorithmType(algorithmType)
        .breakTypeIds(breakTypeGroup.getBreakTypeIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
        .planType(planType)
        .duration(duration)
        .taskStatus(TaskStatus.PUBLISHED)
        .allocationPlanResult(allocationPlanResult)
        .totalBreakNumber(breakContext.getTotalBreakNumber())
        .nextBreakIndex(breakContext.getNextBreakIndex())
        .fileName("")
        .md5("")
        .build());
  }
}
