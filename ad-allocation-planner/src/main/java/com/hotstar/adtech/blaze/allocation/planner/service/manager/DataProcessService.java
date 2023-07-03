package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.algomodel.StandardMatchProgressModel;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class DataProcessService {
  public BreakContext getBreakContext(int totalBreakNumber, int breakIndex, StandardMatchProgressModel model) {
    return model.getEstimatedBreakIndex(breakIndex + 1, totalBreakNumber);
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
      .build();
  }

  public List<DemandDiagnosis> getDemandDiagnosisList(List<AdSet> adSets, BreakContext breakContext,
                                                      Map<Long, Long> adSetImpressions) {

    return adSets.stream()
      .filter(adSet -> !adSet.getSpotAds().isEmpty() || !adSet.getSsaiAds().isEmpty())
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
      .build();
  }

  private double calculateDemand(AdSet adSet, long delivered, BreakContext breakContext) {
    double demandPacingCoefficient = adSet.getDemandPacingCoefficient();
    long target = adSet.getImpressionTarget();

    return getDemandByRatio(target, delivered, breakContext.getExpectedRatio()) * demandPacingCoefficient
      + getDemandByProgress(target, delivered, breakContext.getExpectedProgress()) * (1 - demandPacingCoefficient);
  }

  private double getDemandByRatio(long target, long delivered, double expectedRatio) {
    long remaining = target - delivered;
    return Math.max(expectedRatio * remaining, 0d);
  }

  private double getDemandByProgress(long target, long delivered, double expectedProgress) {
    double actualRatio = (double) delivered / target;
    return Math.max((expectedProgress - actualRatio) * target, 0d);
  }
}
