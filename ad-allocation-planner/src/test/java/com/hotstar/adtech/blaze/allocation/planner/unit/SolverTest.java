package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.hotstar.adtech.blaze.allocation.planner.SolveTestData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.HwmResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm.HwmSolver;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSolver;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SolverTest {
  @Test
  public void testShaleSolverWithLargerData() {
    Random random = new Random();
    random.setSeed(934280);
    int cohortSize = 200;
    int adSetSize = 20;
    int maxConcurrency = 500;
    ShaleSolver shaleSolver = new ShaleSolver();
    GraphContext graphContext = SolveTestData.getGraphContext(random, cohortSize, adSetSize, maxConcurrency);
    List<ShaleResult> solve = shaleSolver.solve(graphContext,
      ShaleConstant.PENALTY);
    solve.forEach(System.out::println);
    Assertions.assertEquals(20, solve.size());
    Map<Long, ShaleResult> resultMap =
      solve.stream().collect(Collectors.toMap(ShaleResult::getId, Function.identity()));
    //    Assertions.assertEquals(0.2872281323269014, resultMap.get(3L).getStd());
    //    Assertions.assertEquals(0.45, resultMap.get(3L).getMean());
    //    Assertions.assertEquals(1.0, resultMap.get(3L).getAlpha());
    //    Assertions.assertEquals(0.0013526903449429257, resultMap.get(3L).getTheta());
    //    Assertions.assertEquals(8.824313841715659E-5, resultMap.get(3L).getSigma());
    //    Assertions.assertEquals(0.007332337439689538, resultMap.get(19L).getSigma());
    //    Assertions.assertEquals(6.569568150963583E-5, resultMap.get(18L).getSigma());
  }

  @Test
  public void testHwmSolverWithLargerData() {
    Random random = new Random();
    random.setSeed(934280);
    int cohortSize = 200;
    int adSetSize = 20;
    int maxConcurrency = 500;
    HwmSolver hwmSolver = new HwmSolver();
    GraphContext graphContext = SolveTestData.getGraphContext(random, cohortSize, adSetSize, maxConcurrency);
    List<HwmResult> solve = hwmSolver.solve(graphContext);
    solve.forEach(System.out::println);
    Assertions.assertEquals(20, solve.size());
    Map<Long, Double> solveResultMap =
      solve.stream().collect(Collectors.toMap(HwmResult::getId, HwmResult::getProbability));
    Assertions.assertEquals(solveResultMap.get(3L), 0.0013526903449429257);
    Assertions.assertEquals(solveResultMap.get(19L), 0.0030328821373359787);
    Assertions.assertEquals(solveResultMap.get(0L), 0.0018913764632662285);
    Assertions.assertEquals(solveResultMap.get(10L), 0.0028587472378032493);
    Assertions.assertEquals(solveResultMap.get(2L), 0.001202410003001201);
    Assertions.assertEquals(solveResultMap.get(5L), 0.0012247728620779948);
    Assertions.assertEquals(solveResultMap.get(15L), 0.0017766247282542374);
  }
}

