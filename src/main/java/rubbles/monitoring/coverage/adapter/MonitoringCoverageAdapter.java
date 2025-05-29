package rubbles.monitoring.coverage.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rubbles.monitoring.coverage.db.DbAdapter;
import rubbles.monitoring.coverage.model.CascadeCountQueryResult;
import rubbles.monitoring.coverage.model.CommunicationCoverageQueryResult;
import rubbles.monitoring.coverage.model.OfferCoverageQueryResult;
import rubbles.monitoring.coverage.model.OfferInfoQueryResult;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MonitoringCoverageAdapter {

    @Autowired
    private DbAdapter dbAdapter;

    @Autowired
    private EmailService emailService;

    @Value("${db.tables.monitoring-recipients}")
    private String MONITORING_RECIPIENTS;

    public void run() throws Exception {

        try {
            // Collecting data for monitoring
            log.info("Getting data for communication coverage monitoring from DB...");

            // Communication coverage data
            List<CommunicationCoverageQueryResult> communicationCoverageData = dbAdapter.selectCommunicationCoverageData();
            if (!communicationCoverageData.isEmpty()) {
                log.info("Successfully received communication coverage data from DB");
                log.debug("Found {} records", communicationCoverageData.size());
                for (CommunicationCoverageQueryResult row : communicationCoverageData) {
                    log.debug("GZ SMS count: {}, GZ Email count: {}, 366 SMS count: {}, 366 EMail count: {}, KF SMS count: {}, KF Email count: {}",
                            row.getGzSmsCount(),
                            row.getGzEmailCount(),
                            row.getAptekaSmsCount(),
                            row.getAptekaEmailCount(),
                            row.getKfSmsCount(),
                            row.getKfEmailCount()
                    );
                }
            } else {
                log.error("Didn't get any communication coverage data from DB");
            }

            // Offer and info data
            List<OfferInfoQueryResult> offerInfoData = dbAdapter.selectOfferInfoData();
            if (!offerInfoData.isEmpty()) {
                log.info("Successfully received offer and info data from DB");
                log.debug("Found {} records", offerInfoData.size());
                for (OfferInfoQueryResult row : offerInfoData) {
                    log.debug("Metric name: {}, GZ SMS: {}, GZ Email: {}, 366 SMS: {}, 366 EMail: {}, KF SMS: {}, KF Email: {}",
                            row.getMetricName(),
                            row.getGzSms(),
                            row.getGzEmail(),
                            row.getAptekaSms(),
                            row.getAptekaEmail(),
                            row.getKfSms(),
                            row.getKfEmail()
                    );
                }
            } else {
                log.error("Didn't get any offer and info data from DB");
            }

            // Offer coverage data
            List<OfferCoverageQueryResult> offerCoverageData = dbAdapter.selectOfferCoverageData();
            if (!offerInfoData.isEmpty()) {
                log.info("Successfully received offer coverage data from DB");
                log.debug("Found {} records", offerCoverageData.size());
                for (OfferCoverageQueryResult row : offerCoverageData) {
                    log.debug("Metric name: {}, GZ count: {}, 366 Count: {}",
                            row.getMetricName(),
                            row.getGzCount(),
                            row.getAptekaCount()
                    );
                }
            } else {
                log.error("Didn't get any offer and info data from DB");
            }

            // Cascade count data
            List<CascadeCountQueryResult> cascadeCountData = dbAdapter.selectCascadeCountData();
            if (!offerInfoData.isEmpty()) {
                log.info("Successfully received cascade count data from DB");
                log.debug("Found {} records", cascadeCountData.size());
                for (CascadeCountQueryResult row : cascadeCountData) {
                    log.debug("Message desc: {}, GZ count: {}, 366 Count: {}",
                            row.getMessageDesc(),
                            row.getGzCount(),
                            row.getAptekaCount()
                    );
                }
            } else {
                log.error("Didn't get any offer and info data from DB");
            }

            // Building email content and sending it ti recipients
            log.info("Building email content");
            String emailContent = buildEmailContent(communicationCoverageData, offerInfoData, offerCoverageData, cascadeCountData);
            log.debug("Email content: {}", emailContent);

            log.info("Getting recipient list from table \"{}\"", MONITORING_RECIPIENTS);
            List<String> recipients = dbAdapter.getRecipients()
                    .stream()
                    .map(row -> (String) row.get("EMAIL"))
                    .toList();
            log.debug("Received {} recipients from DB", recipients.size());

            if (!recipients.isEmpty()) {
                String recipientsList = recipients.stream()
                        .map(email -> "\"" + email + "\"")
                        .collect(Collectors.joining(", "));

                log.debug("Full recipients list: [{}]", recipientsList);

                log.info("Starting sending emails to recipients");
                for (String email : recipients) {
                    try {
                        log.debug("Sending coverage monitoring to recipient with email: {}", email);
                        emailService.sendEmail(email, emailContent);
                        log.debug("Coverage monitoring to recipient with mail \"{}\" has been successfully sent", email);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        try {
                            log.info("Sending error email to recipient with mail: {}", email);
                            emailService.sendErrorEmail(email);
                        } catch (Exception mail) {
                            log.error(mail.getMessage(), mail);
                        }
                    }
                }
                log.info("Finished sending emails to recipients");
            } else {
                log.warn("Recipient list is empty, emails won't be send");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String buildEmailContent(List<CommunicationCoverageQueryResult> communicationCoverageData,
                                    List<OfferInfoQueryResult> offerInfoData,
                                    List<OfferCoverageQueryResult> offerCoverageData,
                                    List<CascadeCountQueryResult> cascadeCountData) {

        final Map<String, String> MONTH_TRANSLATIONS = Map.ofEntries(
                Map.entry("January", "Январь"),
                Map.entry("February", "Февраль"),
                Map.entry("March", "Март"),
                Map.entry("April", "Апрель"),
                Map.entry("May", "Май"),
                Map.entry("June", "Июнь"),
                Map.entry("July", "Июль"),
                Map.entry("August", "Август"),
                Map.entry("September", "Сентябрь"),
                Map.entry("October", "Октябрь"),
                Map.entry("November", "Ноябрь"),
                Map.entry("December", "Декабрь")
        );

        StringBuilder message = new StringBuilder();

        message.append("<html><head><style>")
                .append("  table { border-collapse: collapse; margin: 20px 0; }")
                .append("  th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }")
                .append("  th { background-color: #f2f2f2; }")
                .append("  th.main { background-color: #c0c0c0; text-align: center;}")
                .append("  td.amount { text-align: right; }")
                .append("</style></head><body>")
                .append("<h4>Добрый день!</h4>")
                .append("<p>В таблицах ниже представлены данные по покрытию.</p>");

        message.append(communicationCoverageTableContent(communicationCoverageData));
        message.append(offerInfoTableContent(offerInfoData));
        message.append(offerCoverageTableContent(offerCoverageData));
        message.append(cascadeCountTableContent(cascadeCountData));

        message.append("</body></html>");
        return message.toString();
    }

    private String communicationCoverageTableContent(List<CommunicationCoverageQueryResult> communicationCoverageData) {
        // Заголовок таблицы
        String tableHeader = "Покрытие коммуникациями";

        // Заголовки столбцов таблицы
        String[] headers = {
                "ГЗ SMS",
                "ГЗ Email",
                "366 SMS",
                "366 Email",
                "КФ SMS",
                "КФ Email"
        };

        StringBuilder tableContent = new StringBuilder();

        int filedCount = CommunicationCoverageQueryResult.class.getDeclaredFields().length;

        tableContent.append("<table border='1'>");
        // Заголовок таблицы
        tableContent.append("<tr>").append("<th colspan='").append(filedCount).append("' class=\"main\">").append(tableHeader).append("</th>").append("</tr>");
        // Если нет данных
        if (communicationCoverageData == null || communicationCoverageData.isEmpty()) {
            tableContent.append("<tr><td colspan='").append(filedCount).append("'>Нет данных для отображения</td></tr>");
            return tableContent.toString();
        }
        // Заголовки столбцов таблицы
        tableContent.append("<tr>");
        for (String header : headers) {
            tableContent.append("<th>").append(header).append("</th>");
        }
        tableContent.append("</tr>");
        // Тело таблицы
        for (CommunicationCoverageQueryResult row : communicationCoverageData) {
            tableContent.append("<tr>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getGzSmsCount())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getGzEmailCount())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getAptekaSmsCount())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getAptekaEmailCount())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getKfSmsCount())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getKfEmailCount())).append("</td>")
                    .append("</tr>");
        }
        tableContent.append("</table>");
        return tableContent.toString();
    }

    private String offerInfoTableContent(List<OfferInfoQueryResult> offerInfoData) {
        // Заголовок таблицы
        String tableHeader = "Каскадные рассылки и Мейлы по офферу и инфо";

        // Заголовки столбцов
        String[] headers = {
                "Метрика",
                "ГЗ SMS",
                "ГЗ Email",
                "366 SMS",
                "366 Email",
                "КФ SMS",
                "КФ Email"
        };

        StringBuilder tableContent = new StringBuilder();

        int filedCount = OfferInfoQueryResult.class.getDeclaredFields().length;

        tableContent.append("<table border='1'>");
        // Заголовок таблицы
        tableContent.append("<tr>").append("<th colspan='").append(filedCount).append("' class=\"main\">").append(tableHeader).append("</th>").append("</tr>");
        // Если нет данных
        if (offerInfoData == null || offerInfoData.isEmpty()) {
            tableContent.append("<tr><td colspan='").append(filedCount).append("'>Нет данных для отображения</td></tr>");
            return tableContent.toString();
        }
        // Заголовоки полей таблицы
        tableContent.append("<tr>");
        for (String header : headers) {
            tableContent.append("<th>").append(header).append("</th>");
        }
        tableContent.append("</tr>");

        // Тело таблицы
        for (OfferInfoQueryResult row : offerInfoData) {
            tableContent.append("<tr>")
                    .append("<td>").append(escapeHtml(row.getMetricName())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getGzSms())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getGzEmail())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getAptekaSms())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getAptekaEmail())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getKfSms())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getKfEmail())).append("</td>")
                    .append("</tr>");
        }
        tableContent.append("</table>");

        return tableContent.toString();
    }

    private String offerCoverageTableContent(List<OfferCoverageQueryResult> offerCoverageData) {
        // Заголовок таблицы
        String tableHeader = "Покрытие офферами";

        // Заголовки столбцов
        String[] headers = {
                "Метрика",
                "ГЗ",
                "366"
        };

        StringBuilder tableContent = new StringBuilder();

        int filedCount = OfferCoverageQueryResult.class.getDeclaredFields().length;

        tableContent.append("<table border='1'>");
        // Заголовок таблицы
        tableContent.append("<tr>").append("<th colspan='").append(filedCount).append("' class=\"main\">").append(tableHeader).append("</th>").append("</tr>");
        // Если нет данных
        if (offerCoverageData == null || offerCoverageData.isEmpty()) {
            tableContent.append("<tr><td colspan='").append(filedCount).append("'>Нет данных для отображения</td></tr>");
            return tableContent.toString();
        }
        // Заголовоки полей таблицы
        tableContent.append("<tr>");
        for (String header : headers) {
            tableContent.append("<th>").append(header).append("</th>");
        }
        tableContent.append("</tr>");

        // Тело таблицы
        for (OfferCoverageQueryResult row : offerCoverageData) {
            tableContent.append("<tr>")
                    .append("<td>").append(escapeHtml(row.getMetricName())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getGzCount())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getAptekaCount())).append("</td>")
                    .append("</tr>");
        }
        tableContent.append("</table>");

        return tableContent.toString();
    }

    private String cascadeCountTableContent(List<CascadeCountQueryResult> cascadeCountData) {
        // Заголовок таблицы
        String tableHeader = "Колчество отправленных СМС";

        // Заголовки столбцов
        String[] headers = {
                "Метрика",
                "ГЗ",
                "366"
        };

        StringBuilder tableContent = new StringBuilder();

        int filedCount = CascadeCountQueryResult.class.getDeclaredFields().length;

        tableContent.append("<table border='1'>");
        // Заголовок таблицы
        tableContent.append("<tr>").append("<th colspan='").append(filedCount).append("' class=\"main\">").append(tableHeader).append("</th>").append("</tr>");
        // Если нет данных
        if (cascadeCountData == null || cascadeCountData.isEmpty()) {
            tableContent.append("<tr><td colspan='").append(filedCount).append("'>Нет данных для отображения</td></tr>");
            return tableContent.toString();
        }
        // Заголовоки полей таблицы
        tableContent.append("<tr>");
        for (String header : headers) {
            tableContent.append("<th>").append(header).append("</th>");
        }
        tableContent.append("</tr>");

        // Тело таблицы
        for (CascadeCountQueryResult row : cascadeCountData) {
            tableContent.append("<tr>")
                    .append("<td>").append(escapeHtml(row.getMessageDesc())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getGzCount())).append("</td>")
                    .append("<td class=\"amount\">").append(formatAmount(row.getAptekaCount())).append("</td>")
                    .append("</tr>");
        }
        tableContent.append("</table>");

        return tableContent.toString();
    }

    private static final NumberFormat NUMBER_FORMAT = createNumberFormat();

    private static NumberFormat createNumberFormat() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        numberFormat.setGroupingUsed(true);
        numberFormat.setMaximumFractionDigits(0);
        numberFormat.setMinimumFractionDigits(0);
        return numberFormat;
    }

    private String formatAmount(Long amount) {
        if (amount == null) return "N/A";
        try {
            return NUMBER_FORMAT.format(amount);
        } catch (Exception e) {
            log.error("Error formatting amount", e);
            return "Ошибка";
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
