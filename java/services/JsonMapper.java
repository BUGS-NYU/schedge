package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import models.Course;
import models.Subject;
import models.Term;

import java.io.File;
import java.io.IOException;

public class JsonMapper {
  private static ObjectMapper objMapper = new ObjectMapper();

  public static String toJson(Object o) throws JsonProcessingException {
    return objMapper.writeValueAsString(o);
  }

  public static void toJsonFile(String fileName, Object o) throws IOException {
    objMapper.writeValue(new File(fileName), o);
  }
}
