package com.hotstar.adtech.blaze.exchanger.api.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BreakId {
  String breadId;
  Long timestamp;
}
