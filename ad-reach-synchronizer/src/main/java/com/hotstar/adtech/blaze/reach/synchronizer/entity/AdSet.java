package com.hotstar.adtech.blaze.reach.synchronizer.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdSet {
  long id;
  String contentId;
  long campaignId;
  Boolean maximiseReach;
}
