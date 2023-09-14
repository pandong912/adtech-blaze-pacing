package com.hotstar.adtech.blaze.allocation.diagnosis.sink;

import com.hotstar.adtech.blaze.allocation.diagnosis.config.ClickhouseConnectionStore;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationPlan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AllocationPlanSink {
  private final ClickhouseConnectionStore connectionStore;

  public void writePlan(AllocationPlan allocationPlan) {
    Connection connection = connectionStore.getConnection();
    try (
      PreparedStatement ps = connection.prepareStatement(
        "insert into allocation_plan select si_match_id, version, plan_id,plan_type,break_type,break_duration,"
          + "next_break_index,total_break_number,estimated_model_break_index,"
          + "expected_ratio,expected_progress,algorithm_type "
          + "from input('si_match_id String, version DATETIME64(3),plan_id  Int64, plan_type String, "
          + "break_type String, break_duration Int32,next_break_index Int32, total_break_number Int32, "
          + "estimated_model_break_index Int32,expected_ratio Float64,"
          + "expected_progress Float64,algorithm_type String')")) {
      addPlanInfo(allocationPlan, ps);
      ps.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      connectionStore.closeConnection(connection);
    }
  }

  private void addPlanInfo(AllocationPlan allocationPlan, PreparedStatement ps) {
    try {
      int i = 0;
      ps.setString(++i, allocationPlan.getSiMatchId());
      ps.setObject(++i, LocalDateTime.ofInstant(allocationPlan.getVersion(), ZoneId.of("UTC")));
      ps.setLong(++i, allocationPlan.getPlanId());
      ps.setString(++i, allocationPlan.getPlanType());
      ps.setString(++i, allocationPlan.getBreakType());
      ps.setInt(++i, allocationPlan.getBreakDuration());
      ps.setInt(++i, allocationPlan.getNextBreakIndex());
      ps.setInt(++i, allocationPlan.getTotalBreakNumber());
      ps.setInt(++i, allocationPlan.getEstimatedModelBreakIndex());
      ps.setDouble(++i, allocationPlan.getExpectedRatio());
      ps.setDouble(++i, allocationPlan.getExpectedProgress());
      ps.setString(++i, allocationPlan.getAlgorithmType());
      ps.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
