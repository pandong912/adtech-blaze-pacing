package com.hotstar.adtech.blaze.exchanger.api.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UnReachData {
  Long adSetId;
  Double unReachRatio;
}
