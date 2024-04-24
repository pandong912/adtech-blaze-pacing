package com.hotstar.adtech.blaze.allocation.planner.common.algomodel;

import java.util.List;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

public class StandardMatchProgressModel {

  @Getter
  private final int totalBreakNumber;
  private final Double[] incrementalProgressRatio;
  private final Double[] cumulativeProgressRatio;
  private final Double[] leftCumulativeProgressRatio;

  public StandardMatchProgressModel(List<Double> matchBreakProgressRatios) {
    if (CollectionUtils.isEmpty(matchBreakProgressRatios)) {
      throw new IllegalArgumentException("Match Break Progress Model Data is invalid");
    }

    final double totalProgressRatio = matchBreakProgressRatios.get(matchBreakProgressRatios.size() - 1);
    if (totalProgressRatio < 1.0d) {
      throw new IllegalArgumentException("Match Break Progress Model Data is invalid");
    }

    totalBreakNumber = matchBreakProgressRatios.size();
    incrementalProgressRatio = new Double[totalBreakNumber];
    cumulativeProgressRatio = new Double[totalBreakNumber];
    leftCumulativeProgressRatio = new Double[totalBreakNumber];

    double prevBreakProgressRatio = 0.0d;
    for (int i = 0; i < totalBreakNumber; i++) {
      Double currentBreakProgressRatio = matchBreakProgressRatios.get(i);
      incrementalProgressRatio[i] = currentBreakProgressRatio - prevBreakProgressRatio;
      cumulativeProgressRatio[i] = currentBreakProgressRatio;
      leftCumulativeProgressRatio[i] = totalProgressRatio - prevBreakProgressRatio;
      prevBreakProgressRatio = currentBreakProgressRatio;
    }
  }

  public double getExpectedRatio(int position) {
    return incrementalProgressRatio[position] / leftCumulativeProgressRatio[position];
  }

  public double getExpectedProgress(int position) {
    return cumulativeProgressRatio[position];
  }

}
