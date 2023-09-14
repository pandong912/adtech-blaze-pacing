package com.hotstar.adtech.blaze.allocation.diagnosis.sink;

import com.hotstar.adtech.blaze.allocation.diagnosis.config.ClickhouseConnectionStore;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AdModelAdSetMatch;
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
public class AdModelAdSetMatchSink {
  private final ClickhouseConnectionStore connectionStore;

  public void write(List<AdModelAdSetMatch> adSets) {
    Connection connection = connectionStore.getConnection();
    try (
      PreparedStatement ps = connection.prepareStatement(
        "insert into ad_model_ad_set_match select version, si_match_id, campaign_id,campaign_type,ad_set_id,"
          + "industry_id,brand_id,campaign_status,impression_target,priority, maximise_reach,enabled,break_duration,"
          + "automate_delivery, ssai_ads,spot_ads,aston_ads,audience_targeting_rule_info,stream_targeting_rule_info,"
          + "stream_targeting_rule_type,break_targeting_rule_info,break_targeting_rule_type "
          + "from input('version DateTime64(3), si_match_id String,campaign_id Int64,campaign_type String,"
          + "ad_set_id Int64,industry_id Int32,brand_id Int64,campaign_status String,"
          + "impression_target Int64,priority Int32,"
          + "maximise_reach bool,enabled bool,break_duration Int64,automate_delivery bool,ssai_ads Array(Int64),"
          + "spot_ads Array(Int64),aston_ads Array(Int64),audience_targeting_rule_info String,"
          + "stream_targeting_rule_info String,stream_targeting_rule_type String,"
          + "break_targeting_rule_info Array(Int64), break_targeting_rule_type String')")) {
      adSets.forEach(adSet -> addAdSetMatch(adSet, ps));
      ps.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      connectionStore.closeConnection(connection);
    }
  }

  private void addAdSetMatch(AdModelAdSetMatch adSet, PreparedStatement ps) {
    try {
      int i = 0;
      ps.setObject(++i, LocalDateTime.ofInstant(adSet.getVersion(), ZoneId.of("UTC")));
      ps.setString(++i, adSet.getSiMatchId());
      ps.setLong(++i, adSet.getCampaignId());
      ps.setString(++i, adSet.getCampaignType());
      ps.setLong(++i, adSet.getAdSetId());
      ps.setInt(++i, adSet.getIndustryId());
      ps.setLong(++i, adSet.getBrandId());
      ps.setString(++i, adSet.getCampaignStatus());
      ps.setLong(++i, adSet.getImpressionTarget());
      ps.setInt(++i, adSet.getPriority());
      ps.setObject(++i, adSet.getMaximiseReach());
      ps.setObject(++i, adSet.getEnabled());
      ps.setLong(++i, adSet.getBreakDuration());
      ps.setObject(++i, adSet.getAutomateDelivery());
      ps.setString(++i, adSet.getSsaiAds().toString());
      ps.setString(++i, adSet.getSpotAds().toString());
      ps.setString(++i, adSet.getAstonAds().toString());
      ps.setString(++i, adSet.getAudienceTargetingRuleInfo());
      ps.setString(++i, adSet.getStreamTargetingRuleInfo());
      ps.setString(++i, adSet.getStreamTargetingRuleType());
      ps.setString(++i, adSet.getBreakTargetingRuleInfo().toString());
      ps.setString(++i, adSet.getBreakTargetingRuleType());
      ps.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
