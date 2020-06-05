package scraping.query;

import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.uri.Uri;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class BlackholeCookieStore implements CookieStore {

  public final static BlackholeCookieStore BLACK_HOLE =
      new BlackholeCookieStore();

  private BlackholeCookieStore() {}

  @Override
  public void add(Uri uri, Cookie cookie) {}

  @Override
  public List<Cookie> get(Uri uri) {
    return Collections.emptyList();
  }

  @Override
  public List<Cookie> getAll() {
    return Collections.emptyList();
  }

  @Override
  public boolean remove(Predicate<Cookie> predicate) {
    return false;
  }

  @Override
  public boolean clear() {
    return true;
  }
};
