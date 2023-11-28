package com.hotstar.adtech.blaze.allocation.diagnosis.sink;

import com.hotstar.adtech.blaze.allocation.diagnosis.config.ClickhouseConnectionStore;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationCohortConcurrency;
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
public class AllocationConcurrencySink {
  private final ClickhouseConnectionStore connectionStore;

  public void write(List<AllocationCohortConcurrency> concurrencyList) {
    Connection connection = connectionStore.getConnection();
    try (
      PreparedStatement ps = connection.prepareStatement(
        "insert into allocation_cohort_concurrency select si_match_id, version, content_id,cohort_id,ssai_tag"
          + ",tenant,language,platforms,concurrency,stream_type,playout_id "
          + "from input('si_match_id String, version DATETIME64(3),"
          + "content_id  String,cohort_id Int32, ssai_tag String, tenant String, language String, "
          + "platforms String,concurrency Int64,stream_type String, playout_id String')")) {
      concurrencyList.forEach(
        concurrency -> addCohortConcurrency(concurrency, ps));
      ps.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      connectionStore.closeConnection(connection);
    }
  }

  private void addCohortConcurrency(AllocationCohortConcurrency concurrency, PreparedStatement ps) {
    try {
      int i = 0;
      ps.setString(++i, concurrency.getSiMatchId());
      ps.setObject(++i, LocalDateTime.ofInstant(concurrency.getVersion(), ZoneId.of("UTC")));
      ps.setString(++i, concurrency.getContentId());
      ps.setInt(++i, concurrency.getCohortId());
      ps.setString(++i, concurrency.getSsaiTag());
      ps.setString(++i, concurrency.getTenant());
      ps.setString(++i, concurrency.getLanguage());
      ps.setString(++i, concurrency.getPlatforms());
      ps.setLong(++i, concurrency.getConcurrency());
      ps.setString(++i, String.valueOf(concurrency.getStreamType()));
      ps.setString(++i, String.valueOf(concurrency.getPlayoutId()));
      ps.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
