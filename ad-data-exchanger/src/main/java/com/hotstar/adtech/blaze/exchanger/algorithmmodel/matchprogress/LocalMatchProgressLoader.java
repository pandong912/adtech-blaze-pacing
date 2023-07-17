package com.hotstar.adtech.blaze.exchanger.algorithmmodel.matchprogress;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalMatchProgressLoader implements MatchProgressLoader {

  @Override
  public List<Double> loadModel(LocalDate date) {
    return IntStream.range(1, 51)
        .mapToDouble(i -> 0.02 * i).boxed().collect(Collectors.toList());
  }

}
