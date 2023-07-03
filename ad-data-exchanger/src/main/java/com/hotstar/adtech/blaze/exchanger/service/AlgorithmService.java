package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.exchanger.algorithm.MatchProgressLoader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlgorithmService {
  private final MatchProgressLoader matchProgressLoader;

  public List<Double> getMatchBreakProgressModel(String date) {
    return matchProgressLoader.loadMatchBreakProgress(LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE));
  }

  public List<Double> getLatestMatchBreakProgressModel() {
    return matchProgressLoader.loadMatchBreakProgress();
  }

}
