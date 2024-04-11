package com.hotstar.adtech.blaze.allocation.diagnosis.entity;

import com.hotstar.adtech.blaze.admodel.client.entity.AdEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.LiveEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.MatchEntities;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdModelData {
  Instant version;
  MatchEntities matchEntities;
  LiveEntities liveEntities;
  AdEntities adEntities;
}
