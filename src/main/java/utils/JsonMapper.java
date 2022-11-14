package utils;

import static utils.Try.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonMapper {
  private static ObjectMapper objMapper = new ObjectMapper();

  public static <E> E fromJson(String json, Class<E> clazz) {
    var ctx = Ctx();
    ctx.put("json", json);

    return ctx.log(() -> objMapper.readValue(json, clazz));
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
