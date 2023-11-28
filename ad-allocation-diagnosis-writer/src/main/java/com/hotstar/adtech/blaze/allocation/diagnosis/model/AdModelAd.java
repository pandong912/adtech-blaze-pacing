package com.hotstar.adtech.blaze.allocation.diagnosis.model;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdModelAd {
  Instant version;
  Long id;
  String adId;
  Long adSetId;
  String adType;
  Boolean enabled;
  Integer duration;
  List<Integer> languageIds;


}
