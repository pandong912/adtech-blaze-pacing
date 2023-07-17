package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.adcrash.DistributionsLoader;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.matchprogress.MatchProgressLoader;
import com.hotstar.adtech.blaze.exchanger.api.entity.Distribution;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlgorithmService {
  private final MatchProgressLoader matchProgressLoader;
  private final DistributionsLoader distributionsLoader;

  public List<Double> getMatchBreakProgressModel(LocalDate localDate) {
    return matchProgressLoader.loadModel(localDate);
  }

  /**
   * The latest data is not needed in such scenario,
   * so we read the data two days ago which should have been generated
   */
  public List<Double> getLatestMatchBreakProgressModel() {
    int retryCount = 0;
    LocalDate date = LocalDate.now().minusDays(2);
    while (true) {
      try {
        return matchProgressLoader.loadModel(date);
      } catch (Exception e) {
        retryCount++;
        date = date.minusDays(1);
        if (retryCount > 5) {
          throw new ServiceException("can't read match-progress.csv file", e);
        }
      }
    }
  }

  public List<Distribution> getDistributionModel(LocalDate localDate) {
    return distributionsLoader.loadModel(localDate);
  }

  public List<Distribution> getLatestDistributionModel() {
    LocalDate date = LocalDate.now().minusDays(2);
    return distributionsLoader.loadModel(date);
  }

}
