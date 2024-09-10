package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.admodel.common.enums.PacingMode;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.algomodel.StandardMatchProgressModel;
import com.hotstar.adtech.blaze.allocation.planner.common.utils.DemandUtils;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocationdata.client.model.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class DataProcessService {
  public BreakContext getBreakContext(int totalBreakNumber, int breakIndex, StandardMatchProgressModel model) {
    return getEstimatedBreakIndex(model, breakIndex + 1, totalBreakNumber);
  }

  public Response convertFromDemand(DemandDiagnosis demandDiagnosis) {
    return Response.builder()
      .demandId(demandDiagnosis.getDemandId())
      .adSetId(demandDiagnosis.getAdSetId())
      .order(demandDiagnosis.getOrder())
      .delivered(demandDiagnosis.getDelivered())
      .demand(demandDiagnosis.getDemand())
      .adDuration(demandDiagnosis.getAdDuration())
      .maximizeReach(demandDiagnosis.getMaximizeReach())
      .reachIndex(demandDiagnosis.getReachIndex())
      .remainDelivery(demandDiagnosis.getTarget() - demandDiagnosis.getDelivered())
      .build();
  }

  public List<DemandDiagnosis> getDemandDiagnosisList(List<AdSet> adSets, BreakContext breakContext,
                                                      Map<Long, Long> adSetImpressions) {

    return adSets.stream()
      .map(adSet -> {
        long delivered = adSetImpressions.getOrDefault(adSet.getId(), 0L);
        return convertToDemandDiagnosis(adSet, delivered, breakContext);
      })
      .collect(Collectors.toList());
  }

  private DemandDiagnosis convertToDemandDiagnosis(AdSet adSet, long delivered, BreakContext breakContext) {
    return DemandDiagnosis.builder()
      .adSetId(adSet.getId())
      .demandId(adSet.getDemandId())
      .campaignId(adSet.getCampaignId())
      .campaignType(adSet.getCampaignType())
      .priority(adSet.getPriority())
      .order(adSet.getOrder())
      .target(adSet.getImpressionTarget())
      .delivered(delivered)
      .demand(calculateDemand(adSet, delivered, breakContext))
      .demandPacingCoefficient(adSet.getDemandPacingCoefficient())
      .adDuration((int) IntStream
        .concat(adSet.getSsaiAds().stream().mapToInt(Ad::getDurationMs),
          adSet.getSpotAds().stream().mapToInt(Ad::getDurationMs))
        .average()
        .orElse(0))
      .maximizeReach(adSet.getMaximizeReach())
      .reachIndex(adSet.getReachIndex())
      .build();
  }

  private double calculateDemand(AdSet adSet, long delivered, BreakContext breakContext) {
    long target = adSet.getImpressionTarget();
    if (adSet.getPacingMode() == PacingMode.ASAP) {
      return Math.max(0, target - delivered);
    }
    double demandPacingCoefficient = adSet.getDemandPacingCoefficient();
    double expectedRatio = breakContext.getExpectedRatio();
    double expectedProgress = breakContext.getExpectedProgress();
    return DemandUtils.calculateDemand(demandPacingCoefficient, target, delivered, expectedRatio, expectedProgress);
  }

  private BreakContext getEstimatedBreakIndex(StandardMatchProgressModel model, int nextBreak, int totalBreaks) {
    int totalBreakNumber = model.getTotalBreakNumber();
    // current break index position projected onto the model, start from 1
    int currentBreakIndex =
      Math.min(Double.valueOf(Math.ceil((double) nextBreak * totalBreakNumber / totalBreaks)).intValue(),
        totalBreakNumber);

    int position = currentBreakIndex - 1;
    return BreakContext.builder()
      .nextBreakIndex(nextBreak)
      .totalBreakNumber(totalBreaks)
      .estimatedModelBreakIndex(currentBreakIndex)
      .expectedRatio(model.getExpectedRatio(position))
      .expectedProgress(model.getExpectedProgress(position))
      .build();
  }
}
