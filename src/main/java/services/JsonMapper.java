package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.io.File;
import java.io.IOException;

// @Performance Build a codegen gradle plugin to remove Jackson dependency, and
// do most of serialization legwork at compile time
public class JsonMapper {
  private static ObjectMapper objMapper =
      new ObjectMapper().setPropertyNamingStrategy(
          PropertyNamingStrategy.SNAKE_CASE);

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
