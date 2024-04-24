package com.hotstar.adtech.blaze.allocation.planner.common.utils;

public class DemandUtils {
  public static double calculateDemand(double coeff, long target, long delivered, double expectedRatio,
                                       double expectedProgress) {
    return getDemandByRatio(target, delivered, expectedRatio) * coeff
      + getDemandByProgress(target, delivered, expectedProgress) * (1 - coeff);
  }


  private static double getDemandByRatio(long target, long delivered, double expectedRatio) {
    long remaining = target - delivered;
    return Math.max(expectedRatio * remaining, 0d);
  }

  public static double getDemandByProgress(long target, long delivered, double expectedProgress) {
    double actualRatio = (double) delivered / target;
    return Math.max((expectedProgress - actualRatio) * target, 0d);
  }
}
