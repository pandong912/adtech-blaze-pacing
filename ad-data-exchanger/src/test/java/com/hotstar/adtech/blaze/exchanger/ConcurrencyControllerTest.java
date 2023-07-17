
package com.hotstar.adtech.blaze.exchanger;

import static com.hotstar.adtech.blaze.exchanger.config.CacheConfig.COHORT_CONCURRENCY;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
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
    concurrencyValues.put("P7|SSAI::001", 1L);
    concurrencyValues.put("P1|SSAI::001", 10L);
    concurrencyValues.put("P2|SSAI::001", 100L);
    concurrencyValues.put("P2|SSAI::002", 1000L);
    streamCohortConcurrencyRepository.setStreamCohortConcurrencyBucket("1540018990", "1676628060");
    streamCohortConcurrencyRepository.setContentAllStreamCohortConcurrency("1540018990", "1676628060",
      concurrencyValues);

    Map<String, Long> streamConcurrencyValues = new HashMap<>();
    streamConcurrencyValues.put("P7", 1L);
    streamConcurrencyValues.put("P1", 10L);
    streamConcurrencyValues.put("P2", 1100L);
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
    Assertions.assertEquals(1, map.get("P7|SSAI::001").longValue());
    Assertions.assertEquals(10, map.get("P1|SSAI::001").longValue());
    Assertions.assertEquals(100, map.get("P2|SSAI::001").longValue());
    Assertions.assertEquals(1000, map.get("P2|SSAI::002").longValue());

    List<ContentCohortConcurrencyResponse> cache =
      cacheManager.getCache(COHORT_CONCURRENCY).get("1540018990", List.class);
    Assertions.assertEquals(4, data.size());
    Map<String, Long> cacheMap = cache.stream()
      .collect(Collectors.toMap(ContentCohortConcurrencyResponse::getKey,
        ContentCohortConcurrencyResponse::getConcurrencyValue));
    Assertions.assertEquals(1, map.get("P7|SSAI::001").longValue());
    Assertions.assertEquals(10, map.get("P1|SSAI::001").longValue());
    Assertions.assertEquals(100, map.get("P2|SSAI::001").longValue());
    Assertions.assertEquals(1000, map.get("P2|SSAI::002").longValue());

  }

  @Test
  public void testGetContentStreamWiseConcurrency() {
    List<ContentStreamConcurrencyResponse> data =
      concurrencyController.getContentStreamWiseConcurrency("1540018990").getData();
    Assertions.assertEquals(3, data.size());
    Assertions.assertEquals("P1", data.get(0).getKey());
    Assertions.assertEquals(10, data.get(0).getConcurrencyValue().longValue());
  }

  @Test
  public void testGetContentSingleStreamConcurrency() {
    Long data = concurrencyController.getContentStreamConcurrencyWithPlayoutId("1540018990", "P1").getData();
    Assertions.assertEquals(10L, data.longValue());
  }

  @Test
  public void testInvalidPlayoutId() {
    Assertions.assertThrows(ServiceException.class,
      () -> concurrencyController.getContentStreamConcurrencyWithPlayoutId("1540018990", "1"));
  }
}
