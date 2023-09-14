package com.hotstar.adtech.blaze.allocation.diagnosis.sink;

import com.hotstar.adtech.blaze.allocation.diagnosis.config.ClickhouseConnectionStore;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AdModelMatch;
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
public class AdModelMatchSink {
  private final ClickhouseConnectionStore connectionStore;

  public void write(List<AdModelMatch> matches) {
    Connection connection = connectionStore.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(
      "insert into ad_model_match select version, id, name,si_match_id,content_id,"
        + "language_id,tournament_id,season_id,start_time,status,match_version "
        + "from input('version DateTime64(3),id Int64,name String,si_match_id String,content_id String,"
        + "language_id Int32,tournament_id Int64,season_id Int64,start_time DateTime64(3),"
        + "status String,match_version DateTime64(3)')")) {
      matches.forEach(match -> addMatch(match, ps));
      ps.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      connectionStore.closeConnection(connection);
    }
  }

  private void addMatch(AdModelMatch match, PreparedStatement ps) {
    try {
      int i = 0;
      ps.setObject(++i, LocalDateTime.ofInstant(match.getVersion(), ZoneId.of("UTC")));
      ps.setLong(++i, match.getId());
      ps.setString(++i, match.getName());
      ps.setString(++i, "");
      ps.setString(++i, match.getContentId());
      ps.setInt(++i, match.getLanguageId());
      ps.setLong(++i, match.getTournamentId());
      ps.setLong(++i, match.getSeasonId());
      ps.setObject(++i, LocalDateTime.ofInstant(match.getStartTime(), ZoneId.of("UTC")));
      ps.setString(++i, match.getStatus());
      ps.setObject(++i, LocalDateTime.ofInstant(match.getVersion(), ZoneId.of("UTC")));
      ps.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
