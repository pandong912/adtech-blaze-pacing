package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class ShaleResult {
  private long id;
  private double std;
  private double mean;
  private double alpha;
  private double theta;
  private double sigma;
  private int reachEnabled;
  private int adDuration;
}
