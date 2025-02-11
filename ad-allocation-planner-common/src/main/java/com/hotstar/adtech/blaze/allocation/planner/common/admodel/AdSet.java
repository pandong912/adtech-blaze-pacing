package com.hotstar.adtech.blaze.allocation.planner.common.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.CampaignType;
import com.hotstar.adtech.blaze.admodel.common.enums.PacingMode;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AdSet {

  public static final int MAX_PRIORITY = 5;

  private final long id;
  private final int priority;
  private final String contentId;
  private final long impressionTarget;
  private final double demandPacingCoefficient;
  private final PacingMode pacingMode;

  private final long campaignId;
  private final CampaignType campaignType;

  private final List<Ad> ssaiAds;
  private final List<Ad> spotAds;
  @Builder.Default
  private int maximizeReach = 1;

  private int demandId;
  private int reachIndex;

  public int getOrder() {
    return CampaignType.Promo == campaignType ? priority + MAX_PRIORITY : priority;
  }

}
