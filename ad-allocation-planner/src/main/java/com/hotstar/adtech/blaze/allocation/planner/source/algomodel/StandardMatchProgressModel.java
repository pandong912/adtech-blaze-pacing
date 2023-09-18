package com.hotstar.adtech.blaze.allocation.planner.source.algomodel;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakContext;
import java.util.List;
import org.springframework.util.CollectionUtils;

public class StandardMatchProgressModel {

  private final int totalBreakNumber;
  private final Double[] incrementalProgressRatio;
  private final Double[] cumulativeProgressRatio;
  private final Double[] leftCumulativeProgressRatio;

  public StandardMatchProgressModel(List<Double> matchBreakProgressRatios) {
    if (CollectionUtils.isEmpty(matchBreakProgressRatios)) {
      throw new ServiceException("Match Break Progress Model Data is empty");
    }

    final double totalProgressRatio = matchBreakProgressRatios.get(matchBreakProgressRatios.size() - 1);
    if (totalProgressRatio < 1.0d) {
      throw new ServiceException("Match Break Progress Model Data is invalid");
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

  public BreakContext getEstimatedBreakIndex(int nextBreak, int totalBreaks) {
    // current break index position projected onto the model, start from 1
    int currentBreakIndex =
      Math.min(Double.valueOf(Math.ceil((double) nextBreak * totalBreakNumber / totalBreaks)).intValue(),
        totalBreakNumber);

    int position = currentBreakIndex - 1;
    return BreakContext.builder()
      .nextBreakIndex(nextBreak)
      .totalBreakNumber(totalBreaks)
      .estimatedModelBreakIndex(currentBreakIndex)
      .expectedRatio(getExpectedRatio(position))
      .expectedProgress(getExpectedProgress(position))
      .build();
  }

  public double getExpectedRatio(int position) {
    return incrementalProgressRatio[position] / leftCumulativeProgressRatio[position];
  }

  public double getExpectedProgress(int position) {
    return cumulativeProgressRatio[position];
  }

}
