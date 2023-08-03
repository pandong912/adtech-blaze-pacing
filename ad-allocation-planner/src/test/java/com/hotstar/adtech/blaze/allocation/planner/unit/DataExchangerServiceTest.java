package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataExchangerServiceTest {
  @Mock
  DataExchangerClient dataExchangerClient;

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
