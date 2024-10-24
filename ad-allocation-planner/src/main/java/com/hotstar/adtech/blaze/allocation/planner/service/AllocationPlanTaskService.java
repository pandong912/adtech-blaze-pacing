package com.hotstar.adtech.blaze.allocation.planner.service;

import com.hotstar.adtech.blaze.admodel.common.enums.TaskStatus;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultDetailRepository;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationPlanTaskService {
  private final AllocationPlanResultRepository allocationPlanResultRepository;
  private final AllocationPlanResultDetailRepository allocationPlanResultDetailRepository;

  public AllocationPlanResult getLatestTask(String contentId) {
    return allocationPlanResultRepository.findFirstByContentIdOrderByVersionDesc(contentId);
  }

  public AllocationPlanResult getTask(AllocationPlanResultDetail subtask) {
    return allocationPlanResultRepository
      .findById(subtask.getAllocationPlanResultId())
      .orElseThrow(() -> new RuntimeException("task not found"));
  }

  public List<AllocationPlanResultDetail> getSubTaskList(Long allocationPlanResultId) {
    return allocationPlanResultDetailRepository.findAllByAllocationPlanResultId(allocationPlanResultId);
  }

  public void taskSuccess(AllocationPlanResult allocationPlanResult) {
    allocationPlanResult.setTaskStatus(TaskStatus.SUCCESS);
    allocationPlanResultRepository.save(allocationPlanResult);
  }

  public void taskFailed(AllocationPlanResult allocationPlanResult) {
    allocationPlanResult.setTaskStatus(TaskStatus.FAILED);
    allocationPlanResultRepository.save(allocationPlanResult);
  }

  public void taskExpired(AllocationPlanResult allocationPlanResult) {
    allocationPlanResult.setTaskStatus(TaskStatus.EXPIRED);
    allocationPlanResultRepository.save(allocationPlanResult);
  }

  public void expirePendingSubTask(List<AllocationPlanResultDetail> details) {
    details.stream()
      .filter(detail -> detail.getTaskStatus().isPending())
      .forEach(detail -> detail.setTaskStatus(TaskStatus.EXPIRED));
    allocationPlanResultDetailRepository.saveAll(details);
  }

  public void subTaskExpired(AllocationPlanResultDetail allocationPlanResultDetail) {
    allocationPlanResultDetail.setTaskStatus(TaskStatus.EXPIRED);
    allocationPlanResultDetailRepository.save(allocationPlanResultDetail);
  }

  public void subTaskFail(AllocationPlanResultDetail allocationPlanResultDetail) {
    allocationPlanResultDetail.setTaskStatus(TaskStatus.FAILED);
    allocationPlanResultDetailRepository.save(allocationPlanResultDetail);
  }

  public void subTaskSuccess(AllocationPlanResultDetail subtask, UploadResult uploadResult) {
    subtask.setMd5(uploadResult.getMd5());
    subtask.setTaskStatus(TaskStatus.SUCCESS);
    subtask.setFileName(uploadResult.getFileName());
    subtask.setNextBreakIndex(uploadResult.getNextBreakIndex());
    subtask.setTotalBreakNumber(uploadResult.getTotalBreakNumber());
    allocationPlanResultDetailRepository.save(subtask);
  }


  public Optional<AllocationPlanResultDetail> takeOneSubTask() {
    List<AllocationPlanResultDetail> allocationPlanResultDetails =
      allocationPlanResultDetailRepository
        .findAllByCreatedAtAfterAndTaskStatus(Instant.now().minus(8, ChronoUnit.MINUTES), TaskStatus.PUBLISHED);
    return allocationPlanResultDetails.stream()
      .filter(detail -> detail.getTaskStatus().equals(TaskStatus.PUBLISHED))
      .filter(detail -> allocationPlanResultDetailRepository.updateTaskStatusById(detail.getId()) > 0)
      .findFirst();
  }
}
