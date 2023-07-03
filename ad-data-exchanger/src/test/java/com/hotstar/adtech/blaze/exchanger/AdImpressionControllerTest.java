package com.hotstar.adtech.blaze.exchanger;

import com.hotstar.adtech.blaze.adserver.data.redis.service.ImpressionRepository;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.controller.AdImpressionController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdImpressionControllerTest extends TestEnvConfig {

  @Autowired
  AdImpressionController adImpressionController;

  @Autowired
  CacheManager impressionCacheManager;

  @Autowired
  private ImpressionRepository impressionRepository;

  @BeforeAll
  public void setUp() {
    Map<String, Long> adImpressions = new HashMap<>();
    adImpressions.put("CR-19-000017", 100L);
    adImpressions.put("CR-19-000018", 200L);
    adImpressions.put("CR-19-000019", 300L);
    Map<String, Long> adSetImpressions = new HashMap<>();
    adSetImpressions.put("3325", 100L);
    adSetImpressions.put("3326", 200L);
    adSetImpressions.put("", 200L);
    impressionRepository.setMatchAdImpressions("1440002191", adImpressions);
    impressionRepository.setMatchAdSetImpressions("1440002191", adSetImpressions);
  }


  @Test
  public void testGetAdSetImpression() {
    List<AdSetImpressionResponse> data = adImpressionController.getAllAdSetImpressions("1440002191").getData();
    System.out.println(data);
    Map<Long, AdSetImpressionResponse> map =
      data.stream().collect(Collectors.toMap(AdSetImpressionResponse::getAdSetId, Function.identity()));
    Assertions.assertEquals(2, data.size());
    Assertions.assertEquals(100L, map.get(3325L).getImpression().longValue());
    Assertions.assertEquals(200L, map.get(3326L).getImpression().longValue());

  }


}
