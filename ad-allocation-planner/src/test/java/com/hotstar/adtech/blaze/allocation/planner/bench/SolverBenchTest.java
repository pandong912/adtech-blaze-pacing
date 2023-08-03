package com.hotstar.adtech.blaze.allocation.planner.bench;

import static com.hotstar.adtech.blaze.allocation.planner.SolveTestData.getGraphContext;

import com.hotstar.adtech.blaze.allocation.planner.TestReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm.HwmSolver;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSolver;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang.time.StopWatch;
import org.junit.jupiter.api.Test;

public class SolverBenchTest {
  private static final int PARALLELISM = 1;

  //  @Test
  public void testShaleSolver() {
    Random random = new Random();
    random.setSeed(934280);
    int cohortSize = 50000;
    int adSetSize = 4500;
    int maxConcurrency = 5000;
    StopWatch stopWatch = new StopWatch();
    List<GraphContext> graphContexts =
      IntStream.range(0, PARALLELISM)
        .mapToObj(i -> getGraphContext(random, cohortSize, adSetSize, maxConcurrency))
        .collect(Collectors.toList());
    TestReachStorage testReachStorage = new TestReachStorage(adSetSize, cohortSize);
    long sum = 0;
    for (int j = 0; j < 200; j++) {
      stopWatch.reset();
      stopWatch.start();
      ShaleSolver shaleSolver = new ShaleSolver();
      graphContexts.parallelStream().forEach(graphContext -> shaleSolver.solve(graphContext, testReachStorage,
        ShaleConstant.PENALTY));
      stopWatch.stop();
      sum += stopWatch.getTime();
      System.out.println(stopWatch.getTime());
    }
    System.out.println(sum / 200);
  }

  @Test
  public void testHwmSolver() {
    Random random = new Random();
    random.setSeed(934280);
    int cohortSize = 20000;
    int adSetSize = 300;
    int edgeSize = 100;
    int maxConcurrency = 5000;
    StopWatch stopWatch = new StopWatch();
    List<GraphContext> graphContexts =
      IntStream.range(0, PARALLELISM)
        .mapToObj(i -> getGraphContext(random, cohortSize, adSetSize, maxConcurrency))
        .collect(Collectors.toList());
    stopWatch.start();
    HwmSolver hwmSolver = new HwmSolver();
    graphContexts.parallelStream().forEach(hwmSolver::solve);
    stopWatch.stop();
    System.out.println(stopWatch.getTime());
  }


}

