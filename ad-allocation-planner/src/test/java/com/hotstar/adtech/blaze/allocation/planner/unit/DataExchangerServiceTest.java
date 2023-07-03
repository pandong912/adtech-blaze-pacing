package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.entity.CohortInfo;
import com.hotstar.adtech.blaze.exchanger.api.entity.LanguageMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.PlatformMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import com.hotstar.adtech.blaze.exchanger.api.entity.UnReachData;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataExchangerServiceTest {
  private final LanguageMapping languageMapping = QualificationTestData.getLanguageMapping();
  private final PlatformMapping platformMapping = QualificationTestData.getPlatformMapping();
  @Mock
  DataExchangerClient dataExchangerClient;

  @Test
  public void testGetUnReachRatio() {
    List<CohortInfo> cohortInfos = Arrays.asList(
      CohortInfo.builder()
        .ssaiTag("ssaiTag1")
        .streamDetail(StreamDetail.builder()
          .tenant(Tenant.India)
          .language(languageMapping.getByName("English"))
          .platforms(Arrays.asList(
            platformMapping.getByName("AndroidTV"),
            platformMapping.getByName("FireTV")
          ))
          .build())
        .build(),
      CohortInfo.builder()
        .ssaiTag("ssaiTag2")
        .streamDetail(StreamDetail.builder()
          .tenant(Tenant.India)
          .language(languageMapping.getByName("Hindi"))
          .platforms(Collections.singletonList(
            platformMapping.getByName("Android")
          ))
          .build())
        .build(),
      CohortInfo.builder()
        .ssaiTag("ssaiTag3")
        .streamDetail(StreamDetail.builder()
          .tenant(Tenant.India)
          .language(languageMapping.getByName("Hindi"))
          .platforms(Collections.singletonList(
            platformMapping.getByName("Android")
          ))
          .build())
        .build()
    );

    UnReachData unReachData = UnReachData.builder()
      .unReachRatio(0.5d)
      .adSetId(12345L)
      .build();

    List<UnReachResponse> unReachResponses = Arrays.asList(
      UnReachResponse.builder()
        .ssaiTag("ssaiTag1")
        .streamDetail(StreamDetail.builder()
          .tenant(Tenant.India)
          .language(languageMapping.getByName("English"))
          .platforms(Arrays.asList(
            platformMapping.getByName("AndroidTV"),
            platformMapping.getByName("FireTV")
          ))
          .build())
        .unReachDataList(Collections.singletonList(unReachData))
        .build(),
      UnReachResponse.builder()
        .ssaiTag("ssaiTag2")
        .streamDetail(StreamDetail.builder()
          .tenant(Tenant.India)
          .language(languageMapping.getByName("Hindi"))
          .platforms(Collections.singletonList(
            platformMapping.getByName("Android")
          ))
          .build())
        .unReachDataList(Collections.singletonList(unReachData))
        .build(),
      UnReachResponse.builder()
        .ssaiTag("ssaiTag3")
        .streamDetail(StreamDetail.builder()
          .tenant(Tenant.India)
          .language(languageMapping.getByName("Hindi"))
          .platforms(Collections.singletonList(
            platformMapping.getByName("Android")
          ))
          .build())
        .unReachDataList(Collections.singletonList(unReachData))
        .build()
    );
    Mockito.when(dataExchangerClient.getReachCohortList(Mockito.anyString()))
      .thenReturn(StandardResponse.success(cohortInfos));
    Mockito.when(dataExchangerClient.batchGetUnReachData(Mockito.anyString(), Mockito.anyList()))
      .thenReturn(StandardResponse.success(unReachResponses));
    Map<String, Integer> concurrencyIdMap = new HashMap<>();
    concurrencyIdMap.put("in-English-AndroidTV+FireTV|ssaiTag1", 1);
    concurrencyIdMap.put("in-Hindi-Android|ssaiTag2", 2);
    concurrencyIdMap.put("in-Hindi-Android|ssaiTag4", 3);
    Map<Long, Integer> adSetIdMap = new HashMap<>();
    adSetIdMap.put(12345L, 1);
    adSetIdMap.put(12346L, 2);
    adSetIdMap.put(12347L, 0);

    DataExchangerService dataExchangerService = new DataExchangerService(dataExchangerClient);
    ReachStorage unReachRatio = dataExchangerService.getUnReachRatio("123", concurrencyIdMap, adSetIdMap);
    Assertions.assertEquals(0.5d, unReachRatio.getUnReachRatio(1, 1));
    Assertions.assertEquals(0.5d, unReachRatio.getUnReachRatio(1, 2));
    Assertions.assertEquals(1d, unReachRatio.getUnReachRatio(1, 3));
    Assertions.assertEquals(1d, unReachRatio.getUnReachRatio(2, 1));
  }

  @Test
  public void testGetBreakDefinition() {
    List<BreakTypeResponse> breakTypeResponses = Arrays.asList(
      BreakTypeResponse.builder()
        .type("Spot")
        .id(1)
        .name("Over")
        .duration(40000)
        .durationLowerBound(20000)
        .durationUpperBound(80000)
        .step(20000)
        .build(),
      BreakTypeResponse.builder()
        .id(2)
        .duration(40000)
        .durationLowerBound(20000)
        .durationUpperBound(80000)
        .step(20000)
        .build()
    );
    Mockito.when(dataExchangerClient.getAllBreakType())
      .thenReturn(StandardResponse.success(breakTypeResponses));
    DataExchangerService dataExchangerService = new DataExchangerService(dataExchangerClient);
    List<BreakDetail> breakDefinition = dataExchangerService.getBreakDefinition();
    Assertions.assertEquals(1, breakDefinition.size());
    System.out.println();
    Assertions.assertEquals(Arrays.asList(20000, 40000, 60000, 80000), breakDefinition.get(0).getBreakDuration());
  }
}
