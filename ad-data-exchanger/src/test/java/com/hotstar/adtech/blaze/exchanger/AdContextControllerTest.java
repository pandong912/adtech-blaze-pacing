package com.hotstar.adtech.blaze.exchanger;

import com.hotstar.adtech.blaze.adserver.data.redis.service.RuntimeMatchBreakRepository;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakListResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.BreakTypeResponse;
import com.hotstar.adtech.blaze.exchanger.controller.AdContextController;
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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdContextControllerTest extends TestEnvConfig {
  @Autowired
  private AdContextController adContextController;

  @Autowired
  private RuntimeMatchBreakRepository runtimeMatchBreakRepository;

  @BeforeAll
  public void setUp() {
    runtimeMatchBreakRepository.registerStreamBreak("1440002191", "P1", "SSAI001", 1676628075941L);
    runtimeMatchBreakRepository.registerStreamBreak("1440002191", "P1", "SSAI002", 1676628075941L);
    runtimeMatchBreakRepository.registerStreamBreak("1440002191", "P2", "SSAI001", 1676628075941L);
    runtimeMatchBreakRepository.registerStreamBreak("1440002191", "P3", "SSAI001", 1676628075941L);
  }

  @Test
  public void testGetBreakList() {
    List<BreakListResponse> data = adContextController.getBreakList("1440002191").getData();
    System.out.println(data);
    Assertions.assertEquals(3, data.size());
    Map<String, BreakListResponse> map = data.stream()
      .collect(Collectors.toMap(BreakListResponse::getPlayoutId, Function.identity()));
    Assertions.assertEquals(2, map.get("P1").getBreakIds().size());
    Assertions.assertEquals(1676628075941L, map.get("P1").getBreakIds().get(0).getTimestamp().longValue());
    Assertions.assertEquals(1, map.get("P2").getBreakIds().size());
    Assertions.assertEquals(1, map.get("P3").getBreakIds().size());
  }

  @Test
  public void testGetBreakListByStream() {
    BreakListResponse data = adContextController.getBreakListByStream("1440002191", "P1").getData();
    System.out.println(data);
    Assertions.assertEquals(2, data.getBreakIds().size());
    Assertions.assertEquals(1676628075941L, data.getBreakIds().get(0).getTimestamp().longValue());
  }

  @Test
  public void testGetTotalBreakNumber() {
    Integer data = adContextController.getTotalBreakNumber("1440002191").getData();
    Assertions.assertEquals(65, data.intValue());
  }

  @Test
  public void testGetAllBreakType() {
    List<BreakTypeResponse> data = adContextController.getAllBreakType().getData();
    System.out.println(data);
    Assertions.assertEquals(16, data.size());
    Map<String, BreakTypeResponse> map =
      data.stream().collect(Collectors.toMap(BreakTypeResponse::getName, Function.identity()));
    Assertions.assertEquals(50000, map.get("Over").getDuration().intValue());
  }

}
