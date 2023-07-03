package com.hotstar.adtech.blaze.exchanger.algorithm;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalMatchProgressLoader implements MatchProgressLoader {

  public List<Double> loadMatchBreakProgress() {

    return IntStream.range(1, 51)
      .mapToDouble(i -> 0.02 * i).boxed().collect(Collectors.toList());
  }

  @Override
  public List<Double> loadMatchBreakProgress(LocalDate date) {
    return loadMatchBreakProgress();
  }

}
