package com.hotstar.adtech.blaze.adserver.ingester;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.admodel.common.entity.LanguageEntity;
import com.hotstar.adtech.blaze.admodel.common.entity.PlatformEntity;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.ingester.entity.ConcurrencyGroup;
import com.hotstar.adtech.blaze.adserver.ingester.entity.Match;
import com.hotstar.adtech.blaze.adserver.ingester.service.ConcurrencyService;
import com.hotstar.adtech.blaze.adserver.ingester.service.DataExchangerService;
import com.hotstar.adtech.blaze.adserver.ingester.service.PulseService;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.PlayoutStreamResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConcurrencyServiceTest extends TestEnvConfig {
  @Mock
  private PulseService pulseService;
  @MockBean
  private DataExchangerClient dataExchangerClient;
  @Autowired
  private DataExchangerService dataExchangerService;
  @Autowired
  private StreamConcurrencyRepository streamConcurrencyRepository;
  @Autowired
  private StreamCohortConcurrencyRepository streamCohortConcurrencyRepository;
  private ConcurrencyService concurrencyService;

  @Test
  public void testUpdateStreamCohortConcurrency() {
    Mockito
      .when(pulseService.getLiveContentStreamCohortConcurrency(Mockito.anyString()))
      .thenReturn(getStreamCohortConcurrency());
    Mockito
      .when(pulseService.getLiveContentStreamConcurrency(Mockito.anyString()))
      .thenReturn(getStreamConcurrency());
    Mockito
      .when(dataExchangerClient.getStreamDefinition(Mockito.anyString()))
      .thenReturn(getStreamMapping());
    concurrencyService = new ConcurrencyService(pulseService, dataExchangerService, streamConcurrencyRepository,
      streamCohortConcurrencyRepository);
    concurrencyService.updateMatchConcurrency(new Match("123", "123"));
    Map<String, Long> contentAllStreamConcurrency = streamConcurrencyRepository.getContentAllStreamConcurrency("123");
    System.out.println(contentAllStreamConcurrency);
    Map<String, Long> contentAllStreamCohortConcurrency =
      streamCohortConcurrencyRepository.getContentStreamAllCohortConcurrency("123");
    System.out.println(contentAllStreamCohortConcurrency);
    Assertions.assertEquals(10L, contentAllStreamConcurrency.get("in-Hindi-Android"));
    Assertions.assertEquals(11L, contentAllStreamCohortConcurrency.get("in-Hindi-Android+iOS|SSAI::001"));
    Assertions.assertEquals(2L, contentAllStreamCohortConcurrency.get("in-English-Android+iOS|SSAI:002"));
  }

  private StandardResponse<ContentStreamResponse> getStreamMapping() {
    List<PlayoutStreamResponse> list = Arrays.asList(
      PlayoutStreamResponse.builder()
        .streamDetail(StreamDetail.builder()
          .tenant(Tenant.India)
          .language(LanguageEntity.builder().name("Hindi").build())
          .platforms(Arrays.asList(
            PlatformEntity.builder()
              .name("Android")
              .build(),
            PlatformEntity.builder()
              .name("iOS")
              .build()
          ))
          .build())
        .build(),
      PlayoutStreamResponse.builder()
        .streamDetail(StreamDetail.builder()
          .tenant(Tenant.India)
          .language(LanguageEntity.builder().name("English").build())
          .platforms(Arrays.asList(
            PlatformEntity.builder()
              .name("Android")
              .build(),
            PlatformEntity.builder()
              .name("iOS")
              .build()
          ))
          .build())
        .build()
    );
    ContentStreamResponse contentStreamResponse = ContentStreamResponse.builder()
      .playoutStreamResponses(list)
      .contentId("123")
      .build();
    return StandardResponse.success(contentStreamResponse);
  }

  private ConcurrencyGroup getStreamConcurrency() {
    Map<String, Long> concurrencyValues = new HashMap<>();
    concurrencyValues.put("in-Hindi-Android", 10L);
    concurrencyValues.put("in-Hindi-iOS", 1L);
    concurrencyValues.put("in--iOS|SSAI::001", 1L);
    concurrencyValues.put("in-English-Android", 1L);
    concurrencyValues.put("in-English-tizenTv", 2L);
    return ConcurrencyGroup.builder()
      .tsBucket(100L)
      .concurrencyValues(concurrencyValues)
      .build();
  }

  private ConcurrencyGroup getStreamCohortConcurrency() {
    Map<String, Long> concurrencyValues = new HashMap<>();
    concurrencyValues.put("in-Hindi-Android|SSAI::001", 10L);
    concurrencyValues.put("in-Hindi-iOS|SSAI::001", 1L);
    concurrencyValues.put("in--iOS|SSAI::001", 1L);
    concurrencyValues.put("in-English-Android|SSAI:001", 1L);
    concurrencyValues.put("in-English-Android|SSAI:002", 2L);
    return ConcurrencyGroup.builder()
      .tsBucket(100L)
      .concurrencyValues(concurrencyValues)
      .build();
  }

}
