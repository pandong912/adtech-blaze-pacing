package com.hotstar.adtech.blaze.exchanger.api.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleResultDetail {
  long adSetId;
  double alpha;
  double theta;
  double sigma;
  double mean;
  double std;
  int reachEnabled;
  int adDuration;
}
