package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.google.common.collect.Streams;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.HwmSolveResult;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.HwmAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.GeneralPlanContextLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.HwmPlanWorker;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationplan.client.common.PathUtils;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HwmModeGenerator implements AllocationPlanGenerator {
  private final HwmPlanWorker hwmPlanWorker;
  private final GeneralPlanContextLoader generalPlanContextLoader;
  private final AllocationDiagnosisService allocationDiagnosisService;

  private final AllocationPlanResultRepository allocationPlanResultRepository;

  private final AllocationResultUploader allocationResultUploader;

  @Override
  public void generateAndUploadAllocationPlan(Match match, AdModel adModel) {
    GeneralPlanContext generalPlanContext = generalPlanContextLoader.getGeneralPlanContext(match, adModel);
    List<HwmSolveResult> ssaiShaleSolveResults = hwmPlanWorker.generatePlans(generalPlanContext, PlanType.SSAI);
    List<HwmSolveResult> spotHwmSolveResults = hwmPlanWorker.generatePlans(generalPlanContext, PlanType.SPOT);
    Instant version = Instant.now();
    uploadShaleAndHwmAllocationPlan(match.getContentId(), version, ssaiShaleSolveResults, spotHwmSolveResults);
    Stream<HwmAllocationDiagnosisDetail> ssaiHwmAllocationDiagnosisDetailStream =
      ssaiShaleSolveResults.stream().map(HwmSolveResult::getHwmAllocationDiagnosisDetail);
    Stream<HwmAllocationDiagnosisDetail> spotHwmAllocationDiagnosisDetailStream =
      spotHwmSolveResults.stream().map(HwmSolveResult::getHwmAllocationDiagnosisDetail);
    List<HwmAllocationDiagnosisDetail> hwmAllocationDiagnosisDetails =
      Streams.concat(ssaiHwmAllocationDiagnosisDetailStream, spotHwmAllocationDiagnosisDetailStream)
        .collect(Collectors.toList());
    allocationDiagnosisService.uploadAllocationDiagnosis(match.getContentId(), version, hwmAllocationDiagnosisDetails,
      Collections.emptyList(), generalPlanContext.getConcurrencyData());
  }

  public void uploadShaleAndHwmAllocationPlan(String contentId, Instant version,
                                              List<HwmSolveResult> ssaiAllocationResults,
                                              List<HwmSolveResult> spotAllocationResults) {
    AllocationPlanResult result = AllocationPlanResult.builder()
      .contentId(contentId)
      .version(version)
      .path(PathUtils.joinToPath(contentId, version))
      .build();
    Stream<HwmAllocationPlan> spotHwmAllocationPlanStream =
      spotAllocationResults.stream().map(HwmSolveResult::getHwmAllocationPlan);
    Stream<HwmAllocationPlan> ssaiHwmAllocationPlanStream =
      ssaiAllocationResults.stream().map(HwmSolveResult::getHwmAllocationPlan);
    List<HwmAllocationPlan> hwmAllocationPlans =
      Streams.concat(spotHwmAllocationPlanStream, ssaiHwmAllocationPlanStream)
        .collect(Collectors.toList());

    List<AllocationPlanResultDetail> allocationPlanResultDetails =
      allocationResultUploader.uploadHwmPlan(result, hwmAllocationPlans);

    result.setAllocationPlanResultDetails(allocationPlanResultDetails);
    allocationPlanResultRepository.save(result);
  }


}
