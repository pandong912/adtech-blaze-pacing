package com.hotstar.adtech.blaze.exchanger.algorithmmodel.matchprogress;

import java.time.LocalDate;
import java.util.List;

public interface MatchProgressLoader {

  List<Double> loadModel(LocalDate date);

}
