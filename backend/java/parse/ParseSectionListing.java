package parse;

import org.jsoup.nodes.Element;
import models.SectionAbbrev;

public class ParseSectionListing {
    /**
     * Get formatted course data.
     */
    public static SectionAbbrev parse(Element data) {
        throw new UnsupportedOperationException("");
    }
}

/*
<a href="https://m.albert.nyu.edu/app/catalog/classsection/NYUNV/1198/8699">
    <div class="section-content" style="" data-career="UGRD" data-term="1198"
data-subject="MATH-UA" data-campus="WS" data-days='["M","W"]' data-location="WS"
data-start="9.5" data-end="10.75" data-acad_group="UA" data-rqmnt_designtn=""
data-classID="MATHUA9129903" data-instruct_mode="P" data-acad_org="UAMATH"
data-enrl_stat="O" data-crse_attr="CCAR,CEVL,LEVL"
data-crse_attr_value="CCAR-UG,CEVL-CAS_R,LEVL-U1" data-session="1">
      <i class="ico-arrow-right pull-right right-icon"></i>
      <div class="strong section-body">Section: 001-LEC (8699)</div>
      <div class="section-body">Session: Regular Academic Session</div>
      <div class="section-body">Days/Times: MoWe 9:30am - 10:45am</div>
      <div class="section-body">Dates: 09/03/2019 - 12/13/2019</div>
      <div class="section-body">Instructor: Shizhu Liu</div>
      <div class="section-body">Status: Open</div>
    </div>
</a>
*/
