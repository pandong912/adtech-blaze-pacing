
package com.hotstar.adtech.blaze.exchanger;

import static com.hotstar.adtech.blaze.exchanger.config.CacheConfig.COHORT_CONCURRENCY;

import com.hotstar.adtech.blaze.admodel.common.enums.Platform;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.controller.ConcurrencyController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ConcurrencyControllerTest extends TestEnvConfig {
  @Autowired
  private ConcurrencyController concurrencyController;
  @Autowired
  private StreamCohortConcurrencyRepository streamCohortConcurrencyRepository;
  @Autowired
  private StreamConcurrencyRepository streamConcurrencyRepository;
  @Autowired
  CacheManager cacheManager;

  @BeforeAll
  public void setUp() {
    Map<String, Long> concurrencyValues = new HashMap<>();
    concurrencyValues.put("in-Hindi-Android|SSAI::001", 1L);
    concurrencyValues.put("in-English-iOS|SSAI::001", 10L);
    concurrencyValues.put("in-English-JioLyf|SSAI::001", 100L);
    concurrencyValues.put("in-English-JioLyf|SSAI::002", 1000L);
    streamCohortConcurrencyRepository.setStreamCohortConcurrencyBucket("1540018990", "1676628060");
    streamCohortConcurrencyRepository.setContentAllStreamCohortConcurrency("1540018990", "1676628060",
      concurrencyValues);

    Map<String, Long> streamConcurrencyValues = new HashMap<>();
    streamConcurrencyValues.put("in-Hindi-Android", 1L);
    streamConcurrencyValues.put("in-English-iOS", 10L);
    streamConcurrencyValues.put("in-English-JioLyf", 1100L);
    streamConcurrencyRepository.setStreamConcurrencyBucket("1540018990", "1676628060");
    streamConcurrencyRepository.setContentAllStreamConcurrency("1540018990", "1676628060", streamConcurrencyValues);
  }

  @Test
  public void testGetContentCohortWiseConcurrency() {
    List<ContentCohortConcurrencyResponse> data =
      concurrencyController.getContentCohortWiseConcurrency("1540018990").getData();
    Assertions.assertEquals(4, data.size());
    Map<String, Long> map = data.stream()
      .collect(Collectors.toMap(ContentCohortConcurrencyResponse::getKey,
        ContentCohortConcurrencyResponse::getConcurrencyValue));
    Assertions.assertEquals(1, map.get("in-Hindi-Android|SSAI::001").longValue());
    Assertions.assertEquals(10, map.get("in-English-iOS|SSAI::001").longValue());
    Assertions.assertEquals(100, map.get("in-English-JioLyf|SSAI::001").longValue());
    Assertions.assertEquals(1000, map.get("in-English-JioLyf|SSAI::002").longValue());

    List<ContentCohortConcurrencyResponse> cache =
      cacheManager.getCache(COHORT_CONCURRENCY).get("1540018990", List.class);
    Assertions.assertEquals(4, data.size());
    Map<String, Long> cacheMap = cache.stream()
      .collect(Collectors.toMap(ContentCohortConcurrencyResponse::getKey,
        ContentCohortConcurrencyResponse::getConcurrencyValue));
    Assertions.assertEquals(1, cacheMap.get("in-Hindi-Android|SSAI::001").longValue());
    Assertions.assertEquals(10, cacheMap.get("in-English-iOS|SSAI::001").longValue());
    Assertions.assertEquals(100, cacheMap.get("in-English-JioLyf|SSAI::001").longValue());
    Assertions.assertEquals(1000, cacheMap.get("in-English-JioLyf|SSAI::002").longValue());
  }

  @Test
  public void testGetContentStreamWiseConcurrency() {
    List<ContentStreamConcurrencyResponse> data =
      concurrencyController.getContentStreamWiseConcurrency("1540018990").getData();
    Assertions.assertEquals(3, data.size());
    Assertions.assertEquals("in-Hindi-Android", data.get(0).getKey());
    Assertions.assertEquals(1, data.get(0).getConcurrencyValue().longValue());
  }

  @Test
  public void testGetContentSingleStreamConcurrency() {
    Long data = concurrencyController.getContentSingleStreamConcurrency("1540018990", Tenant.India, "English",
      Platform.iOS.toString()).getData();
    Assertions.assertEquals(10L, data.longValue());
  }
}
