package com.hotstar.adtech.blaze.allocation.diagnosis.config;

import com.clickhouse.jdbc.ClickHouseDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ClickhouseConnectionStore {
  @Value("${blaze.clickhouse.url}")
  private String clickhouseUrl;
  @Value("${blaze.clickhouse.username}")
  private String userName;
  @Value("${blaze.clickhouse.password}")
  private String password;
  @Value("${blaze.clickhouse.driver-class-name}")
  private String driverClassName;
  private DataSource clickhouseDataSource;

  @PostConstruct
  public void intiClickhouseDataSource() {
    try {
      HikariConfig hikariConfig = new HikariConfig();
      hikariConfig.setDataSource(buildClickhouseDataSource());
      hikariConfig.setPoolName("clickhouseDataSourcePool");
      hikariConfig.setMaximumPoolSize(30);
      hikariConfig.setMinimumIdle(10);
      hikariConfig.setConnectionTimeout(120000);
      hikariConfig.setDriverClassName(driverClassName);
      clickhouseDataSource = new HikariDataSource(hikariConfig);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Connection getConnection() {
    try {
      return clickhouseDataSource.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void closeConnection(Connection connection) {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private ClickHouseDataSource buildClickhouseDataSource() throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", userName);
    properties.setProperty("password", password);
    return new ClickHouseDataSource(clickhouseUrl, properties);
  }
}
