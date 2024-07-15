package com.hotstar.adtech.blaze.reach.synchronizer;

import com.hotstar.adtech.blaze.pacing.redis.ReachDataRepository;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.Match;
import com.hotstar.adtech.blaze.reach.synchronizer.repository.DecisionReachDataRepository;
import com.hotstar.adtech.blaze.reach.synchronizer.service.ReachService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReachServiceTest extends TestEnvConfig {
  @Autowired
  private ReachDataRepository reachDataRepository;
  @Autowired
  private DecisionReachDataRepository decisionReachDataRepository;
  @Autowired
  private ReachService reachService;

  private final String tsBucket = getDefaultTsBucket();

  @BeforeAll
  public void init() {
    HashMap<String, Double> reachValues = new HashMap<>();
    reachValues.put("17000", 0.1);
    reachValues.put("17001", 0.2);
    reachValues.put("17002", 0.3);
    reachValues.put("17003", 0.3);
    reachDataRepository.setContentCohortReachRatio("123", tsBucket, "P7|SSAI::001", reachValues);
    reachDataRepository.setContentCohortReachRatio("123", tsBucket, "P1|SSAI::002", reachValues);
  }

  private String getDefaultTsBucket() {
    return String.valueOf((Instant.now().minus(4, ChronoUnit.MINUTES).getEpochSecond() / 60) * 60);
  }

  @Test
  public void test() throws InterruptedException {
    Map<Long, Boolean> adSetMaximiseReach = new HashMap<>();
    adSetMaximiseReach.put(17000L, true);
    adSetMaximiseReach.put(17001L, true);
    adSetMaximiseReach.put(17002L, false);
    Match match = new Match("123", "123");
    reachService.updateMatchReachMatch(match, adSetMaximiseReach);
    Thread.sleep(10000);
    Map<String, Double> contentCohortReachRatio =
      decisionReachDataRepository.getContentCohortReachRatio("123", "P7|SSAI::001");
    Map<String, Double> contentCohortReachRatio1 =
      decisionReachDataRepository.getContentCohortReachRatio("123", "P1|SSAI::002");
    String tsBucket = decisionReachDataRepository.getTsBucket("123");
    System.out.println("tsBucket = " + tsBucket);
    System.out.println("contentCohortReachRatio = " + contentCohortReachRatio);
    System.out.println("contentCohortReachRatio1 = " + contentCohortReachRatio1);
    Assertions.assertEquals(0.1, contentCohortReachRatio.get("17000"));
    Assertions.assertEquals(0.2, contentCohortReachRatio.get("17001"));
    Assertions.assertNull(contentCohortReachRatio.get("17002"));
    Assertions.assertNull(contentCohortReachRatio.get("17003"));
    Assertions.assertEquals(0.1, contentCohortReachRatio1.get("17000"));
    Assertions.assertEquals(0.2, contentCohortReachRatio1.get("17001"));
  }

}
