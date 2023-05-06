package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.HwmSolveResult;
import com.hotstar.adtech.blaze.allocation.planner.common.response.ShaleSolveResult;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.HwmAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ShaleAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.ShalePlanContextLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.HwmPlanWorker;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.ShalePlanWorker;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import com.hotstar.adtech.blaze.allocationplan.client.common.PathUtils;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShaleAndHwmModeGenerator implements AllocationPlanGenerator {

  private final ShalePlanWorker shalePlanWorker;
  private final HwmPlanWorker hwmPlanWorker;
  private final ShalePlanContextLoader shalePlanContextLoader;
  private final AllocationDiagnosisService allocationDiagnosisService;
  private final AllocationResultUploader allocationResultUploader;
  private final AllocationPlanResultRepository allocationPlanResultRepository;


  @Override
  public void generateAndUploadAllocationPlan(Match match, AdModel adModel) {
    ShalePlanContext shalePlanContext = shalePlanContextLoader.getShalePlanContext(match, adModel);
    List<ShaleSolveResult> ssaiShaleSolveResults = shalePlanWorker.generatePlans(shalePlanContext, PlanType.SSAI);
    List<HwmSolveResult> spotHwmSolveResults =
      hwmPlanWorker.generatePlans(shalePlanContext.getGeneralPlanContext(), PlanType.SPOT);
    Instant version = Instant.now();
    uploadShaleAndHwmAllocationPlan(match.getContentId(), version, ssaiShaleSolveResults,
      spotHwmSolveResults);
    List<HwmAllocationDiagnosisDetail> spotHwmAllocationDiagnosisDetail =
      spotHwmSolveResults.stream().map(HwmSolveResult::getHwmAllocationDiagnosisDetail).collect(Collectors.toList());
    List<ShaleAllocationDiagnosisDetail> ssaiHwmAllocationDiagnosisDetail =
      ssaiShaleSolveResults.stream().map(ShaleSolveResult::getShaleAllocationDiagnosisDetail)
        .collect(Collectors.toList());
    allocationDiagnosisService.uploadAllocationDiagnosis(match.getContentId(), version,
      spotHwmAllocationDiagnosisDetail, ssaiHwmAllocationDiagnosisDetail,
      shalePlanContext.getGeneralPlanContext().getConcurrencyData());
  }

  public void uploadShaleAndHwmAllocationPlan(String contentId, Instant version,
                                              List<ShaleSolveResult> ssaiAllocationResults,
                                              List<HwmSolveResult> spotAllocationResults) {
    AllocationPlanResult result = AllocationPlanResult.builder()
      .contentId(contentId)
      .version(version)
      .path(PathUtils.joinToPath(contentId, version))
      .build();
    List<HwmAllocationPlan> spotAllocationPlans =
      spotAllocationResults.stream().map(HwmSolveResult::getHwmAllocationPlan).collect(Collectors.toList());
    List<ShaleAllocationPlan> ssaiAllocationPlans =
      ssaiAllocationResults.stream().map(ShaleSolveResult::getShaleAllocationPlan).collect(Collectors.toList());

    List<AllocationPlanResultDetail> ssaiAllocationPlanResultDetails =
      allocationResultUploader.uploadShalePlan(result, ssaiAllocationPlans);
    List<AllocationPlanResultDetail> spotAllocationPlanResultDetails =
      allocationResultUploader.uploadHwmPlan(result, spotAllocationPlans);

    ssaiAllocationPlanResultDetails.addAll(spotAllocationPlanResultDetails);

    result.setAllocationPlanResultDetails(ssaiAllocationPlanResultDetails);
    allocationPlanResultRepository.save(result);
  }
}
