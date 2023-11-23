package com.hotstar.adtech.blaze.allocationplan.client.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffUtils {
  private static final ThreadLocal<LinkedBuffer> bufferCache =
    ThreadLocal.withInitial(() -> LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

  private static final IdStrategy strategy = new DefaultIdStrategy(
    IdStrategy.DEFAULT_FLAGS | IdStrategy.COLLECTION_SCHEMA_ON_REPEATED_FIELDS, null, 0);

  public static <T> byte[] serialize(T obj) {
    byte[] data;
    if (obj == null) {
      return null;
    }
    Class<T> clazz = (Class<T>) obj.getClass();
    Schema<T> schema = getSchema(clazz);
    try {
      data = ProtostuffIOUtil.toByteArray(obj, schema, bufferCache.get());
    } finally {
      bufferCache.get().clear();
    }
    return data;
  }

  public static <T> T deserialize(byte[] data, Class<T> clazz) {
    Schema<T> schema = getSchema(clazz);
    T obj = schema.newMessage();
    ProtostuffIOUtil.mergeFrom(data, obj, schema);
    return obj;
  }

  private static <T> Schema<T> getSchema(Class<T> clazz) {
    return RuntimeSchema.getSchema(clazz, strategy);
  }
}
