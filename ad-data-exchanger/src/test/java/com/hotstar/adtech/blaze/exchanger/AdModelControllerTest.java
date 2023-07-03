package com.hotstar.adtech.blaze.exchanger;

import static com.hotstar.adtech.blaze.exchanger.config.CacheConfig.SEASON_ID_BY_CONTENT;

import com.hotstar.adtech.blaze.admodel.common.domain.ResultCode;
import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.admodel.repository.AdModelResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AdModelResult;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.TournamentInfoResponse;
import com.hotstar.adtech.blaze.exchanger.controller.AdModelController;
import java.time.Instant;
import java.util.Collections;
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
public class AdModelControllerTest extends TestEnvConfig {
  @Autowired
  private AdModelController adModelController;
  @Autowired
  CacheManager databaseCacheManager;

  @Autowired
  private AdModelResultRepository adModelResultRepository;

  @BeforeAll
  public void setup() {
    AdModelResult adModelResult = AdModelResult.builder()
      .version(Instant.ofEpochMilli(1662015630113L))
      .path("test")
      .bucket("test")
      .adModelResultDetails(Collections.emptyList())
      .build();
    adModelResultRepository.save(adModelResult);
  }

  @Test
  public void testGetStreamDefinition() {
    ContentStreamResponse data = adModelController.getStreamDefinition("1540018990").getData();
    System.out.println(data);
    Assertions.assertEquals(1, data.getPlayoutStreamResponses().size());
    Assertions.assertEquals("P1", data.getPlayoutStreamResponses().get(0).getPlayoutId());
    Assertions.assertEquals("in-English-Web+MWeb+JioLyf",
      data.getPlayoutStreamResponses().get(0).getStreamDetail().getKey());

    ContentStreamResponse defaultData = adModelController.getStreamDefinition("-111").getData();
    System.out.println(defaultData);
    Assertions.assertEquals(17, defaultData.getPlayoutStreamResponses().size());

  }

  @Test
  public void testGetLatestAdModel() {
    AdModelResultUriResponse data = adModelController.getLatestAdModel(-1L).getData();
    Assertions.assertEquals("test", data.getPath());
    Assertions.assertEquals(1662015630113L, data.getVersion().longValue());
    StandardResponse<AdModelResultUriResponse> latestAdModel = adModelController.getLatestAdModel(1662015630113L);
    Assertions.assertEquals(ResultCode.FAILURE, latestAdModel.getCode());
  }

  @Test
  public void testGetAdModel() {
    AdModelResultUriResponse data = adModelController.getAdModel(1662015630113L).getData();
    Assertions.assertEquals("test", data.getPath());
    Assertions.assertEquals(1662015630113L, data.getVersion().longValue());
  }

  @Test
  public void testGetSeasonIdByContentId() {
    TournamentInfoResponse data = adModelController.getSeasonIdByContentId("1440002191").getData();
    System.out.println(data);
    Assertions.assertEquals(634, data.getSeasonId().longValue());
    TournamentInfoResponse cache =
      databaseCacheManager.getCache(SEASON_ID_BY_CONTENT).get("1440002191", TournamentInfoResponse.class);
    assert cache != null;
    Assertions.assertEquals(634, cache.getSeasonId().longValue());
  }
}
