package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MANAGER_PUBLISH;

import com.hotstar.adtech.blaze.admodel.common.enums.TaskStatus;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroupFactory;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationplan.client.common.PathUtils;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskPublisher {
  private final AllocationPlanResultRepository allocationPlanResultRepository;
  private final BreakTypeGroupFactory breakTypeGroupFactory;

  @Timed(value = MANAGER_PUBLISH)
  public void publish(Match match, GeneralPlanContext generalPlanContext, Instant version,
                      Function<BreakTypeGroup, Stream<AllocationPlanResultDetail>> spotTaskBuilder,
                      List<Function<BreakTypeGroup, Stream<AllocationPlanResultDetail>>> ssaiTaskBuilders) {
    AllocationPlanResult allocationPlanResult = AllocationPlanResult.builder()
      .contentId(match.getContentId())
      .version(version)
      .path(PathUtils.joinToPath(match.getContentId(), version))
      .allocationPlanResultDetails(new ArrayList<>())
      .taskStatus(TaskStatus.PUBLISHED)
      .build();

    List<BreakTypeGroup> breakTypeList =
      breakTypeGroupFactory.getBreakTypeList(generalPlanContext.getAdSets(), generalPlanContext.getBreakDetails());

    List<AllocationPlanResultDetail> ssaiAllocationPlanResultDetails = ssaiTaskBuilders.stream()
      .flatMap(ssaiTaskBuilder -> breakTypeList.stream().flatMap(ssaiTaskBuilder))
      .collect(Collectors.toList());
    List<AllocationPlanResultDetail> spotAllocationPlanResultDetails =
      breakTypeList.stream().flatMap(spotTaskBuilder).collect(Collectors.toList());

    allocationPlanResult.getAllocationPlanResultDetails().addAll(ssaiAllocationPlanResultDetails);
    allocationPlanResult.getAllocationPlanResultDetails().addAll(spotAllocationPlanResultDetails);
    allocationPlanResult.getAllocationPlanResultDetails()
      .forEach(detail -> {
        detail.setAllocationPlanResult(allocationPlanResult);
        detail.setTotalBreakNumber(generalPlanContext.getBreakContext().getTotalBreakNumber());
        detail.setNextBreakIndex(generalPlanContext.getBreakContext().getNextBreakIndex());
        detail.setFileName("");
        detail.setMd5("");
      });
    allocationPlanResultRepository.save(allocationPlanResult);
  }
}
