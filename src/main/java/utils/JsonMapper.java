package utils;

import static utils.TryCatch.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonMapper {
  private static ObjectMapper objMapper = new ObjectMapper();

  public static <E> E fromJson(String json, Class<E> clazz) {
    return tcFatal(()
                       -> objMapper.readValue(json, clazz),
                   "Failed to parse JSON (value={})", json);
  }
  public static <E> List<E> fromJsonArray(String json, Class<E> clazz) {
    CollectionType type =
        objMapper.getTypeFactory().constructCollectionType(List.class, clazz);

    return tcFatal(()
                       -> objMapper.readValue(json, type),
                   "Failed to parse JSON (value={})", json);
  }

  public static String toJson(Object o) { return toJson(o, false); }

  public static void toJsonFile(String fileName, Object o) {
    toJsonFile(fileName, o, false);
  }

  public static String toJson(Object o, boolean prettyPrint) {
    try {
      if (prettyPrint)
        return objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
      else
        return objMapper.writeValueAsString(o);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void toJsonFile(String fileName, Object o,
                                boolean prettyPrint) {
    try {
      if (prettyPrint)
        objMapper.writerWithDefaultPrettyPrinter().writeValue(
            new File(fileName), o);
      else
        objMapper.writeValue(new File(fileName), o);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
