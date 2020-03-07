package actions;

import nyu.Term;

public final class UpdateData {
  public static void updateData() {
    Term currentTerm = Term.getCurrentTerm();
    Term nextTerm = currentTerm.nextTerm();
    Term nextNextTerm = nextTerm.nextTerm();

    ScrapeTerm.scrapeTerm(currentTerm, 20, 100);
    ScrapeTerm.scrapeTerm(nextTerm, 20, 100);
    ScrapeTerm.scrapeTerm(nextNextTerm, 20, 100);
  }
}
