package com.hotstar.adtech.blaze.exchanger;

import com.hotstar.adtech.blaze.adserver.data.redis.service.ReachDataRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UnReachTest extends TestEnvConfig {
  @Autowired
  private ReachDataRepository reachDataRepository;

  @Test
  public void testGetUnReachData() {
    Map<Long, Double> reachData = new HashMap<>();
    reachData.put(1L, 0.1);
    reachData.put(2L, 0.2);
    reachDataRepository.setContentCohortReachRatio("1540018990", "1676628060", "in-Hind-Android|SSAI::001",
      reachData);
    Map<Long, Double> contentAllCohortReachRatio =
      reachDataRepository.getContentCohortReachRatio("1540018990", "1676628060", "in-Hind-Android|SSAI::001");
    System.out.println(contentAllCohortReachRatio);

  }
}