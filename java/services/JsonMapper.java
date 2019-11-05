package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.io.File;
import java.io.IOException;

public class JsonMapper {
  private static ObjectMapper objMapper =
      new ObjectMapper().setPropertyNamingStrategy(
          PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

  public static String toJson(Object o) throws JsonProcessingException {
    return objMapper.writeValueAsString(o);
  }

  public static void toJsonFile(String fileName, Object o) throws IOException {
    objMapper.writeValue(new File(fileName), o);
  }
}
