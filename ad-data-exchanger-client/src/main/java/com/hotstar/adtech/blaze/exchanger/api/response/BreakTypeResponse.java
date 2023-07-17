package com.hotstar.adtech.blaze.exchanger.api.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BreakTypeResponse {
  Integer id;
  String name;
  Integer duration;
  String type;
  Integer durationLowerBound;
  Integer durationUpperBound;
  Integer step;
}
