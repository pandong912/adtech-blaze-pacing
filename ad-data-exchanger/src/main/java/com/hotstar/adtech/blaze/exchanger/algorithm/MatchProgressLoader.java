package com.hotstar.adtech.blaze.exchanger.algorithm;

import java.time.LocalDate;
import java.util.List;

public interface MatchProgressLoader {

  List<Double> loadMatchBreakProgress();

  List<Double> loadMatchBreakProgress(LocalDate date);

}
