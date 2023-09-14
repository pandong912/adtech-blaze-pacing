package com.hotstar.adtech.blaze.allocation.diagnosis.sink;

import com.hotstar.adtech.blaze.allocation.diagnosis.config.ClickhouseConnectionStore;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationAdSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AllocationAdSetSink {
  private final ClickhouseConnectionStore connectionStore;

  public void writeAdSet(List<AllocationAdSet> adSets) {
    Connection connection = connectionStore.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(
      "insert into allocation_ad_set select si_match_id, version, plan_id,ad_set_id,order,demand,target,campaign_id,"
        + "delivered,total_supply,cohort_list,probability,theta,alpha,sigma,mean"
        + " from input('si_match_id String, version DATETIME64(3),"
        + "plan_id Int64, ad_set_id Int64, order Int32, demand Float64, "
        + "target Int64, campaign_id Int64, delivered Int64,total_supply Int64,"
        + "cohort_list AggregateFunction(groupBitmap, UInt32),probability Float64, theta Float64,"
        + " alpha Float64, sigma Float64,mean Float64')")) {
      adSets.forEach(
        adSet -> addAdSetDiagnosis(adSet, ps));
      ps.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      connectionStore.closeConnection(connection);
    }
  }

  private void addAdSetDiagnosis(AllocationAdSet adSet, PreparedStatement ps) {
    try {
      int i = 0;
      ps.setString(++i, adSet.getSiMatchId());
      ps.setObject(++i, LocalDateTime.ofInstant(adSet.getVersion(), ZoneId.of("UTC")));
      ps.setLong(++i, adSet.getPlanId());
      ps.setLong(++i, adSet.getAdSetId());
      ps.setInt(++i, adSet.getOrder());
      ps.setDouble(++i, adSet.getDemand());
      ps.setLong(++i, adSet.getTarget());
      ps.setLong(++i, adSet.getCampaignId());
      ps.setLong(++i, adSet.getDelivered());
      ps.setLong(++i, 0);
      ps.setObject(++i, new int[1]);
      ps.setDouble(++i, adSet.getProbability());
      ps.setDouble(++i, adSet.getTheta());
      ps.setDouble(++i, adSet.getAlpha());
      ps.setDouble(++i, adSet.getSigma());
      ps.setDouble(++i, adSet.getMean());
      ps.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
