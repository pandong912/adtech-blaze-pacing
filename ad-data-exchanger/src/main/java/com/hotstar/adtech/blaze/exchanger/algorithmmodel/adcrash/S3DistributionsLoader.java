package com.hotstar.adtech.blaze.exchanger.algorithmmodel.adcrash;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.exchanger.api.entity.Distribution;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public class S3DistributionsLoader implements DistributionsLoader {
  private final AmazonS3 s3Client;
  private final String bucket;
  private final String path;

  public S3DistributionsLoader(AmazonS3 s3Client, String bucket, String path) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.path = path;
  }

  @Override
  public List<Distribution> loadModel(LocalDate date) {
    try (InputStream in = doLoadFromS3(date); CSVReader csvReader = csvReaderFrom(in)) {
      List<String[]> lines = csvReader.readAll();
      return buildModel(lines);
    } catch (IOException | CsvException e) {
      throw new ServiceException("can't read match-progress.csv file", e);
    }
  }

  private S3ObjectInputStream doLoadFromS3(LocalDate date) {
    String day = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String finalPath = path.replace("{date}", day);
    return s3Client.getObject(bucket, finalPath).getObjectContent();
  }

  private static CSVReader csvReaderFrom(InputStream in) {
    return new CSVReader(new BufferedReader(new InputStreamReader(in)));
  }

  private static List<Distribution> buildModel(List<String[]> lines) {
    if (CollectionUtils.isEmpty(lines)) {
      return Collections.emptyList();
    }
    return lines.stream()
        // skip csv header
        .skip(1)
        .map(line -> {
          double seconds = Double.parseDouble(line[0]);
          double probability = Double.parseDouble(line[1]);
          return Distribution.builder()
              .breakDurationMs((int) (seconds * 1000))
              .probability(probability)
              .build();
        })
        .collect(Collectors.toList());
  }

}
