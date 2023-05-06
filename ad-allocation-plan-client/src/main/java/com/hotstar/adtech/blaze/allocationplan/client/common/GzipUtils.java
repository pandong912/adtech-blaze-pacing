package com.hotstar.adtech.blaze.allocationplan.client.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {
  public static byte[] compress(byte[] data) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(out);
    gzip.write(data);
    gzip.close();
    gzip.finish();
    return out.toByteArray();
  }

  public static byte[] decompress(byte[] data) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayInputStream in = new ByteArrayInputStream(data);
    GZIPInputStream gzip = new GZIPInputStream(in);
    byte[] buffer = new byte[1024];
    int n;
    while ((n = gzip.read(buffer)) >= 0) {
      out.write(buffer, 0, n);
    }
    return out.toByteArray();
  }
}
