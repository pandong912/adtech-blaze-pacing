package com.hotstar.adtech.blaze.exchanger.algorithmmodel.adcrash;

import com.hotstar.adtech.blaze.exchanger.api.entity.Distribution;
import java.time.LocalDate;
import java.util.List;

public interface DistributionsLoader {

  List<Distribution> loadModel(LocalDate date);

}
