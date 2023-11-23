package com.hotstar.adtech.blaze.allocationdata.client.util;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {
  public static byte[] compress(byte[] data) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      GZIPOutputStream gzip = new GZIPOutputStream(out);
      gzip.write(data);
      gzip.close();
      gzip.finish();
      return out.toByteArray();
    } catch (Exception e) {
      throw new ServiceException("fail to compress data", e);
    }
  }

  public static byte[] decompress(byte[] data) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      GZIPInputStream gzip = new GZIPInputStream(in);
      byte[] buffer = new byte[1024];
      int n;
      while ((n = gzip.read(buffer)) >= 0) {
        out.write(buffer, 0, n);
      }
      return out.toByteArray();
    } catch (Exception e) {
      throw new ServiceException("fail to decompress data", e);
    }
  }
}
