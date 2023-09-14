package com.hotstar.adtech.blaze.allocation.diagnosis.sink;

import com.hotstar.adtech.blaze.allocation.diagnosis.config.ClickhouseConnectionStore;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AdModelAd;
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
public class AdModelAdSink {
  private final ClickhouseConnectionStore connectionStore;

  public void write(List<AdModelAd> adModelAds) {
    Connection connection = connectionStore.getConnection();
    try (
      PreparedStatement ps = connection.prepareStatement(
        "insert into ad_model_ad select version, id, ad_id,ad_type,ad_set_id,"
          + "enabled,duration,language_ids "
          + "from input('version DateTime64(3), id Int64,ad_id String,ad_type String,"
          + "ad_set_id Int64,enabled bool,duration Int32,language_ids Array(Int64)')")) {
      adModelAds.forEach(ad -> addAd(ad, ps));
      ps.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      connectionStore.closeConnection(connection);
    }
  }

  private void addAd(AdModelAd ad, PreparedStatement ps) {
    try {
      int i = 0;
      ps.setObject(++i, LocalDateTime.ofInstant(ad.getVersion(), ZoneId.of("UTC")));
      ps.setLong(++i, ad.getId());
      ps.setString(++i, ad.getAdId());
      ps.setString(++i, ad.getAdType());
      ps.setLong(++i, ad.getAdSetId());
      ps.setObject(++i, ad.getEnabled());
      ps.setInt(++i, ad.getDuration());
      ps.setString(++i, ad.getLanguageIds().toString());
      ps.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
