package com.hotstar.adtech.blaze.ingester.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Match {
  String contentId;
  Long tournamentId;
  Long seasonId;
}
