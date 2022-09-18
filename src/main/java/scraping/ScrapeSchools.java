package scraping;

import java.util.*;

/* User flow

  1.  Navigate to

        https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL

      and get cookies

  2.  Navigate there again, and get more cookies
  3.  Navigate there again using POST, and set form params



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

  public static scrapeSchools() {}
}
