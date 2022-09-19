package scraping;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;
import org.asynchttpclient.*;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.uri.Uri;
import org.slf4j.*;

/* User flow

  1.  Navigate to

        https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL

      and get cookies

  2.  Navigate there again, and get more cookies
  3.  Navigate there again using POST, and set form params



  src/main/java/utils/Client.java
 */

public final class ScrapeSchools {
  public static final class Subject {
    public String code;
    public String name;
  }

  public static final class School {
    public String code;
    public String name;
    public ArrayList<Subject> subjects;
  }

  public static scrapeSchools(Term term) {}
}
