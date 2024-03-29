package com.hotstar.adtech.blaze.allocationdata.client.model;

import com.hotstar.adtech.blaze.admodel.common.enums.CampaignType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandDiagnosis {
  private final long adSetId;
  private final int demandId;
  private final long campaignId;
  private final CampaignType campaignType;
  private final int priority;
  private final int order;
  private final long target;
  private final long delivered;
  private final double demand;
  private final double demandPacingCoefficient;
  private final int adDuration;
  private final int maximizeReach;
  private final int reachIndex;
}
