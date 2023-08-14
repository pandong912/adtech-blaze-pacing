package com.hotstar.adtech.blaze.allocation.planner.service;

import com.amazonaws.util.CollectionUtils;
import com.hotstar.adtech.blaze.admodel.common.enums.TaskStatus;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultDetailRepository;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationPlanTaskService {
  private final AllocationPlanResultRepository allocationPlanResultRepository;
  private final AllocationPlanResultDetailRepository allocationPlanResultDetailRepository;

  public AllocationPlanResult getLatestTask(String contentId) {
    return allocationPlanResultRepository.findFirstByContentIdOrderByVersionDesc(contentId);
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


  @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public Optional<AllocationPlanResultDetail> takeOneSubTask() {
    List<AllocationPlanResultDetail> allocationPlanResultDetails =
      allocationPlanResultDetailRepository.findAllByTaskStatus(TaskStatus.PUBLISHED);
    if (CollectionUtils.isNullOrEmpty(allocationPlanResultDetails)) {
      return Optional.empty();
    } else {
      Collections.shuffle(allocationPlanResultDetails);
      AllocationPlanResultDetail detail = allocationPlanResultDetails.get(0);
      detail.setTaskStatus(TaskStatus.IN_PROGRESS);
      allocationPlanResultDetailRepository.save(detail);
      return Optional.of(detail);
    }
  }
}
