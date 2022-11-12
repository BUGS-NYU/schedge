package test;

import api.*;
import database.*;
import io.javalin.*;

public class Util {
  public static Javalin app;

  static {
    // Force the connection to initialize with migrations
    GetConnection.withConnection(conn -> Migrations.runMigrations(conn));
    app = App.makeApp();
  }
}
