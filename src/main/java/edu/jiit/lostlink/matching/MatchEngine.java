package edu.jiit.lostlink.matching;

import edu.jiit.lostlink.model.ItemReport;
import edu.jiit.lostlink.service.ReportService;
import java.sql.SQLException;
import java.util.List;

/** Small facade for the explainable weighted matching logic used by the application. */
public class MatchEngine {
    private final ReportService reports;
    public MatchEngine(ReportService reports){this.reports=reports;}
    public List<ItemReport> findMatches(ItemReport report)throws SQLException{return reports.matches(report);}
    public int compatibilityScore(ItemReport first,ItemReport second){return reports.score(first,second);}
}
