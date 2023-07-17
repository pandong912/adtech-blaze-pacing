package com.hotstar.adtech.blaze.ingester;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.admodel.common.entity.LanguageEntity;
import com.hotstar.adtech.blaze.admodel.common.enums.Ladder;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDefinition;
import com.hotstar.adtech.blaze.ingester.entity.ConcurrencyGroup;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.service.ConcurrencyService;
import com.hotstar.adtech.blaze.ingester.service.DataExchangerService;
import com.hotstar.adtech.blaze.ingester.service.PulseService;
import java.util.Arrays;
import java.util.Collections;
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
      .when(dataExchangerClient.getStreamDefinitionV2(Mockito.anyString()))
      .thenReturn(getStreamMapping());
    concurrencyService = new ConcurrencyService(pulseService, dataExchangerService, streamConcurrencyRepository,
      streamCohortConcurrencyRepository);
    concurrencyService.updateMatchConcurrency(new Match("123", "123"));
    Map<String, Long> contentAllStreamConcurrency = streamConcurrencyRepository.getContentAllStreamConcurrency("123");
    System.out.println("contentAllStreamConcurrency: " + contentAllStreamConcurrency);
    Assertions.assertEquals(1L, contentAllStreamConcurrency.get("in-hin--ssai"));
    Assertions.assertEquals(3L, contentAllStreamConcurrency.get("P15"));
    Assertions.assertEquals(10L, contentAllStreamConcurrency.get("P7"));

    Map<String, Long> contentAllStreamCohortConcurrency =
      streamCohortConcurrencyRepository.getContentStreamAllCohortConcurrency("123");
    System.out.println("contentAllStreamCohortConcurrency: " + contentAllStreamCohortConcurrency);
    Assertions.assertEquals(1L, contentAllStreamCohortConcurrency.get("in--eng|SSAI::001"));
    Assertions.assertEquals(1L, contentAllStreamCohortConcurrency.get("P15|SSAI::001"));
    Assertions.assertEquals(11L, contentAllStreamCohortConcurrency.get("P1|SSAI::001"));
    Assertions.assertEquals(12L, contentAllStreamCohortConcurrency.get("P15|SSAI::002"));
  }

  private StandardResponse<List<StreamDefinition>> getStreamMapping() {
    List<StreamDefinition> list = Arrays.asList(
      StreamDefinition.builder()
        .playoutId("P1")
        .streamType(StreamType.SSAI_Spot)
        .ladders(Collections.singletonList(Ladder.phone))
        .language(LanguageEntity.builder().abbreviation("eng").build())
        .tenant(Tenant.India)
        .build(),
      StreamDefinition.builder()
        .playoutId("P7")
        .streamType(StreamType.SSAI_Spot)
        .ladders(Collections.singletonList(Ladder.phone))
        .language(LanguageEntity.builder().abbreviation("hin").build())
        .tenant(Tenant.India)
        .build(),
      StreamDefinition.builder()
        .playoutId("P15")
        .streamType(StreamType.Spot)
        .ladders(Arrays.asList(Ladder.phone, Ladder.tv))
        .language(LanguageEntity.builder().abbreviation("tel").build())
        .tenant(Tenant.India)
        .build()
    );
    return StandardResponse.success(list);
  }

  private ConcurrencyGroup getStreamConcurrency() {
    Map<String, Long> concurrencyValues = new HashMap<>();
    concurrencyValues.put("in-hin-phone-ssai", 10L);
    concurrencyValues.put("in-hin--ssai|SSAI::001", 1L);
    concurrencyValues.put("in-tel-phone-non_ssai", 1L);
    concurrencyValues.put("in-tel-tv-non_ssai", 2L);
    return ConcurrencyGroup.builder()
      .tsBucket(100L)
      .concurrencyValues(concurrencyValues)
      .build();
  }

  private ConcurrencyGroup getStreamCohortConcurrency() {
    Map<String, Long> concurrencyValues = new HashMap<>();
    concurrencyValues.put("in--eng|SSAI::001", 1L);
    concurrencyValues.put("in-eng-phone-ssai|SSAI::001|English+Android", 1L);
    concurrencyValues.put("in-eng-phone-ssai|SSAI::001|English+iOS", 10L);
    concurrencyValues.put("in-eng-phone-ssai|SSAI::002", 1L);
    concurrencyValues.put("in-tel-phone-non_ssai|SSAI::002", 1L);
    concurrencyValues.put("in-tel-tv-non_ssai|SSAI::002", 11L);
    concurrencyValues.put("in-tel-tv-non_ssai|SSAI::001", 1L);
    return ConcurrencyGroup.builder()
      .tsBucket(100L)
      .concurrencyValues(concurrencyValues)
      .build();
  }

}
