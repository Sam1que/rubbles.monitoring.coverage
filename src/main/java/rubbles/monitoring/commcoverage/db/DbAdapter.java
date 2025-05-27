package rubbles.monitoring.commcoverage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rubbles.monitoring.commcoverage.common.db.DbService;
import rubbles.monitoring.commcoverage.model.OfferInfoQueryResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DbAdapter {

    @Autowired
    @Qualifier("cdmDbService")
    private DbService cdmDbService;

    @Value("${db.tables.monitoring-recipients}")
    private String MONITORING_RECIPIENTS;

    @Value("${sql.select-offer-info-query}")
    private String selectOfferInfoQuery;

    @Value("${sql.select-recipients-query}")
    private String selectRecipientsQuery;

    public List<Map<String, Object>> getRecipients() throws Exception {
        try {
            return cdmDbService.select(replaceSql(selectRecipientsQuery), (Map<String, Object>) null);
        } catch (Exception e) {
            throw new Exception("Error selecting from monitoring recipients table: " + MONITORING_RECIPIENTS + e.getMessage());
        }
    }

    public List<OfferInfoQueryResult> selectOfferInfoData() throws Exception {
        try {
            String query = selectOfferInfoQuery;
            List<Map<String, Object>> rows = cdmDbService.select(query, new HashMap<>());
            return rows.stream().map(row -> new OfferInfoQueryResult(
                    (String) row.get("METRIC_NAME"),
                    (Long) row.get("GZ_SMS"),
                    (Long) row.get("GZ_EMAIL"),
                    (Long) row.get("366_SMS"),
                    (Long) row.get("366_EMAIL"),
                    (Long) row.get("KF_SMS"),
                    (Long) row.get("KF_EMAIL")
            )).collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Error selecting data from database" + e.getMessage());
        }
    }

    private String replaceSql(String sql) {
        return sql
                .replace("&monitoring_recipients", MONITORING_RECIPIENTS);
    }
}
