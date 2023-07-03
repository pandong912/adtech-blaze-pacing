package com.hotstar.adtech.blaze.exchanger.algorithm;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public class S3MatchProgressLoader implements MatchProgressLoader {
  private final String path;
  private final String bucket;
  private final AmazonS3 s3Client;

  public S3MatchProgressLoader(AmazonS3 s3Client, String bucket, String path) {
    this.s3Client = s3Client;
    this.path = path;
    this.bucket = bucket;
  }

  public List<Double> loadMatchBreakProgress() {
    try (InputStream in = loadLatestMatchProgressFromS3(); CSVReader csvReader = csvReaderFrom(in)) {
      return buildMatchProgress(csvReader);
    } catch (IOException | CsvException e) {
      throw new ServiceException("can't read match-progress.csv file", e);
    }
  }

  @Override
  public List<Double> loadMatchBreakProgress(LocalDate date) {
    try (InputStream in = doLoadMatchProgressFromS3(date); CSVReader csvReader = csvReaderFrom(in)) {
      return buildMatchProgress(csvReader);
    } catch (IOException | CsvException e) {
      throw new ServiceException("can't read match-progress.csv file", e);
    }
  }

  private static List<Double> buildMatchProgress(CSVReader csvReader) throws IOException, CsvException {
    List<String[]> lines = csvReader.readAll();

    if (CollectionUtils.isEmpty(lines)) {
      throw new ServiceException("No data found in match progress file");
    }
    List<Double> matchBreakProgresses = lines.stream()
      // skip csv header
      .skip(1)
      .filter(line -> line != null && line.length > 1)
      .sorted(Comparator.comparing(line -> Integer.parseInt(line[0].trim())))
      .map(line -> Double.parseDouble(line[1].trim()))
      .collect(Collectors.toList());
    List<Double> truncate = new ArrayList<>();

    for (Double matchBreakProgress : matchBreakProgresses) {
      truncate.add(matchBreakProgress);
      if (matchBreakProgress >= 1.0d) {
        break;
      }
    }
    return truncate;
  }

  /**
   * The latest data is not needed in such scenario,
   * so we read the data two days ago which should have been generated
   */
  private S3ObjectInputStream loadLatestMatchProgressFromS3() {
    int retryCount = 0;
    LocalDate date = LocalDate.now().minusDays(2);
    while (true) {
      try {
        return doLoadMatchProgressFromS3(date);
      } catch (Exception e) {
        log.error("Failed to load match progress from s3, retrying. date:" + date, e);
        retryCount++;
        date = date.minusDays(1);
        if (retryCount > 5) {
          throw new ServiceException("can't read match-progress.csv file", e);
        }
      }
    }
  }

  private S3ObjectInputStream doLoadMatchProgressFromS3(LocalDate date) {
    String day = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String finalPath = path.replace("{date}", day);
    return s3Client.getObject(bucket, finalPath).getObjectContent();
  }

  private CSVReader csvReaderFrom(InputStream in) {
    return new CSVReader(new BufferedReader(new InputStreamReader(in)));
  }
}
