package com.hotstar.adtech.blaze.allocation.planner.source.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import com.hotstar.adtech.blaze.allocationplan.client.common.GzipUtils;
import com.hotstar.adtech.blaze.allocationplan.client.common.ProtostuffUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

@Slf4j
@SuppressWarnings("unused")
public class S3AllocationDataClient implements AllocationDataClient {

  private static final String ALLOCATION_PLAN_DATA = "allocation-plan-data";
  private static final String SHALE_PLAN_DATA = "shale-plan-data";
  private static final String HWM_PLAN_DATA = "hwm-plan-data";
  private static final int S3_LOAD_BUF_SIZE = 1024 * 4;
  private static final int MAX_CONNECTIONS = 6;

  private final String bucketName;
  private final AmazonS3 s3Client;

  public S3AllocationDataClient(String s3Region, String bucketName) {
    this.bucketName = bucketName;
    this.s3Client = AmazonS3ClientBuilder.standard()
      .withRegion(s3Region)
      .withClientConfiguration(new ClientConfiguration().withMaxConnections(MAX_CONNECTIONS))
      .build();
  }

  private String doUpload(byte[] file, String path, String fileName) {
    String key = Paths.get(path, fileName).toString();
    byte[] compressed = GzipUtils.compress(file);
    try (InputStream is = new ByteArrayInputStream(compressed)) {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(compressed.length);
      s3Client.putObject(this.bucketName, key, is, metadata);
    } catch (IOException e) {
      throw new ServiceException("Failed to upload allocation data to:" + bucketName + "/" + key, e);
    }
    return fileName;
  }

  private <T> T loadFromS3(Class<T> clazz, String path, String fileName) {
    String key =
      Paths.get(path, fileName).toString();
    try (InputStream is = loadFromS3(key)) {
      byte[] bytes = IOUtils.toByteArray(is);
      byte[] decompress = GzipUtils.decompress(bytes);
      return ProtostuffUtils.deserialize(decompress, clazz);
    } catch (Exception e) {
      throw new ServiceException("Failed to load allocation data from:" + bucketName + "/" + key, e);
    }
  }

  private InputStream loadFromS3(String keyName) {
    S3Object s3Object = s3Client.getObject(bucketName, keyName);
    S3ObjectInputStream s3is = s3Object.getObjectContent();
    return new BufferedInputStream(s3is, S3_LOAD_BUF_SIZE);
  }


  @Override
  public void uploadShaleData(String contentId, String version, ShalePlanContext shalePlanContext) {
    byte[] file = ProtostuffUtils.serialize(shalePlanContext);
    String path = Paths.get(ALLOCATION_PLAN_DATA, contentId, version).toString();
    String fileName = doUpload(file, path, SHALE_PLAN_DATA);
  }

  @Override
  public void uploadHwmData(String contentId, String version, GeneralPlanContext generalPlanContext) {
    byte[] file = ProtostuffUtils.serialize(generalPlanContext);
    String path = Paths.get(ALLOCATION_PLAN_DATA, contentId, version).toString();
    String fileName = doUpload(file, path, HWM_PLAN_DATA);
  }

  @Override
  public ShalePlanContext loadShaleData(String contentId, String version) {
    String path = Paths.get(ALLOCATION_PLAN_DATA, contentId, version).toString();
    return loadFromS3(ShalePlanContext.class, path, SHALE_PLAN_DATA);
  }

  @Override
  public GeneralPlanContext loadHwmData(String contentId, String version) {
    String path = Paths.get(ALLOCATION_PLAN_DATA, contentId, version).toString();
    return loadFromS3(GeneralPlanContext.class, path, HWM_PLAN_DATA);
  }
}
