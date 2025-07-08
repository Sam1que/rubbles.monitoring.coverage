package rubbles.monitoring.coverage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rubbles.monitoring.coverage.common.db.DbService;
import rubbles.monitoring.coverage.model.CascadeCountQueryResult;
import rubbles.monitoring.coverage.model.AvailableClientBaseQueryResult;
import rubbles.monitoring.coverage.model.OfferCoverageQueryResult;
import rubbles.monitoring.coverage.model.CommunicationCoverageQueryResult;

import java.math.BigDecimal;
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

    @Value("${sql.select-available-client-base-query}")
    private String selectAvailableClientBaseQuery;

    @Value("${sql.select-communication-coverage-query}")
    private String selectCommunicationCoverageQuery;

    @Value("${sql.select-offer-coverage-query}")
    private String selectOfferCoverageQuery;

    @Value("${sql.select-cascade-count-query}")
    private String selectCascadeCountQuery;

    @Value("${sql.select-recipients-query}")
    private String selectRecipientsQuery;

    public List<Map<String, Object>> getRecipients() throws Exception {
        try {
            return cdmDbService.select(replaceSql(selectRecipientsQuery), (Map<String, Object>) null);
        } catch (Exception e) {
            throw new Exception("Error selecting from monitoring recipients table: " + MONITORING_RECIPIENTS + e.getMessage());
        }
    }

    public List<AvailableClientBaseQueryResult> selectAvailableClientBaseData() throws Exception {
        try {
            String query = selectAvailableClientBaseQuery;
            List<Map<String, Object>> rows = cdmDbService.select(query, new HashMap<>());
            return rows.stream().map(row -> new AvailableClientBaseQueryResult(
                    (String) row.get("brand"),
                    (Long) row.get("total_clients"),
                    (Long) row.get("available_clients"),
                    (Long) row.get("email_count"),
                    (Long) row.get("sms_count"),
                    (Long) row.get("email_and_sms_count"),
                    (String) row.get("email_percentage"),
                    (String) row.get("sms_percentage"),
                    (String) row.get("email_and_sms_percentage")
            )).collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Error selecting data from database" + e.getMessage());
        }
    }

    public List<CommunicationCoverageQueryResult> selectCommunicationCoverageData() throws Exception {
        try {
            String query = selectCommunicationCoverageQuery;
            List<Map<String, Object>> rows = cdmDbService.select(query, new HashMap<>());
            return rows.stream().map(row -> new CommunicationCoverageQueryResult(
                    (String) row.get("brand"),
                    (String) row.get("type"),
                    (String) row.get("channel"),
                    (Long) row.get("client_count"),
                    (Long) row.get("unique_client_count"),
                    (BigDecimal) row.get("communications_per_client"),
                    (String) row.get("coverage")
            )).collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Error selecting data from database" + e.getMessage());
        }
    }

    public List<OfferCoverageQueryResult> selectOfferCoverageData() throws Exception {
        try {
            String query = selectOfferCoverageQuery;
            List<Map<String, Object>> rows = cdmDbService.select(query, new HashMap<>());
            return rows.stream().map(row -> new OfferCoverageQueryResult(
                    (String) row.get("brand"),
                    (Long) row.get("offer_count"),
                    (Long) row.get("unique_clients_with_offer"),
                    (Long) row.get("offers_with_comm"),
                    (Long) row.get("unique_clients_with_offer_and_comm"),
                    (Long) row.get("available_base"),
                    (String) row.get("offer_coverage"),
                    (String) row.get("offer_with_comm_coverage")
            )).collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Error selecting data from database" + e.getMessage());
        }
    }

    public List<CascadeCountQueryResult> selectCascadeCountData() throws Exception {
        try {
            String query = selectCascadeCountQuery;
            List<Map<String, Object>> rows = cdmDbService.select(query, new HashMap<>());
            return rows.stream().map(row -> new CascadeCountQueryResult(
                    (String) row.get("MESSAGE_DESC"),
                    (Long) row.get("GZ"),
                    (Long) row.get("366"),
                    (Long) row.get("total")
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
