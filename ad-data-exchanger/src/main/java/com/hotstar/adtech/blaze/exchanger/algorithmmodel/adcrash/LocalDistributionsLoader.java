package com.hotstar.adtech.blaze.exchanger.algorithmmodel.adcrash;

import com.hotstar.adtech.blaze.exchanger.api.entity.Distribution;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalDistributionsLoader implements DistributionsLoader {

  @Override
  public List<Distribution> loadModel(LocalDate date) {
    int size = 70;
    return IntStream.range(10, 10 + size)
        .mapToObj(i -> Distribution.builder().breakDurationMs(i * 1000).probability(0.1).build())
        .collect(Collectors.toList());
  }

}
