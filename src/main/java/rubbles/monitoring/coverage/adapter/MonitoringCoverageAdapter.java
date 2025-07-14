package rubbles.monitoring.coverage.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rubbles.monitoring.coverage.db.DbAdapter;
import rubbles.monitoring.coverage.model.CascadeCountQueryResult;
import rubbles.monitoring.coverage.model.AvailableClientBaseQueryResult;
import rubbles.monitoring.coverage.model.OfferCoverageQueryResult;
import rubbles.monitoring.coverage.model.CommunicationCoverageQueryResult;

import java.text.NumberFormat;
import java.util.Collections;
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

    public void run() {
        List<AvailableClientBaseQueryResult> availableClientBaseDate = Collections.emptyList();
        List<CommunicationCoverageQueryResult> communicationCoverageData = Collections.emptyList();
        List<OfferCoverageQueryResult> offerCoverageData = Collections.emptyList();
        List<CascadeCountQueryResult> cascadeCountData = Collections.emptyList();

        try {
            // Collecting data for monitoring
            log.info("Getting data for monitoring from DB...");

            // Available client base data
            try {
                log.info("Getting available client base data...");
                availableClientBaseDate = dbAdapter.selectAvailableClientBaseData();
                if (!availableClientBaseDate.isEmpty()) {
                    log.info("Successfully received available client base data ({} records).", availableClientBaseDate.size());
                    for (AvailableClientBaseQueryResult row : availableClientBaseDate) {
                        log.debug("Brand: {}, Total Clients: {}, Available Clients: {}, Email Count: {}, SMS Count: {}, Email & SMS Count: {}, Email %: {}, SMS %: {}, Email & SMS %: {}",
                                row.getBrand(),
                                row.getTotalClientsCount(),
                                row.getAvailableClientCount(),
                                row.getEmailCount(),
                                row.getSmsCount(),
                                row.getEmailAndSmsCount(),
                                row.getEmailPercentage(),
                                row.getSmsPercentage(),
                                row.getEmailAndSmsPercentage()
                        );
                    }
                } else {
                    log.warn("Didn't get any available client base data from DB.");
                }
            } catch (Exception e) {
                log.error("Error getting available client base data from DB: {}", e.getMessage(), e);
            }

            // Communication coverage data
            try {
                log.info("Getting communication coverage data...");
                communicationCoverageData = dbAdapter.selectCommunicationCoverageData();
                if (!communicationCoverageData.isEmpty()) {
                    log.info("Successfully received communication coverage data ({} records).", communicationCoverageData.size());
                    for (CommunicationCoverageQueryResult row : communicationCoverageData) {
                        log.debug("Brand: {}, Type: {}, Channel: {}, Client Count: {}, Unique Client Count: {}, Communications Per Client: {}, Coverage: {}",
                                row.getBrand(),
                                row.getType(),
                                row.getChannel(),
                                row.getCommCount(),
                                row.getUniqueClientCount(),
                                row.getCommunicationsPerClient(),
                                row.getCoverage()
                        );
                    }
                } else {
                    log.warn("Didn't get any communication coverage data from DB.");
                }
            } catch (Exception e) {
                log.error("Error getting communication coverage data from DB: {}", e.getMessage(), e);
            }

            // Offer coverage data
            try {
                log.info("Getting offer coverage data...");
                offerCoverageData = dbAdapter.selectOfferCoverageData();
                if (!offerCoverageData.isEmpty()) {
                    log.info("Successfully received offer coverage data ({} records).", offerCoverageData.size());
                    for (OfferCoverageQueryResult row : offerCoverageData) {
                        log.debug("Brand: {}, Offer count: {}, Unique clients with offer: {}, Offers with comm: {}, Unique clients with offer and comm: {}, Available base: {}, Offer coverage: {}, Offer with comm coverage: {}",
                                row.getBrand(),
                                row.getOfferCount(),
                                row.getUniqueClientsWithOffer(),
                                row.getOffersWithComm(),
                                row.getUniqueClientsWithOfferAndComm(),
                                row.getAvailableBase(),
                                row.getOfferCoverage(),
                                row.getOfferWithCommCoverage()
                        );
                    }
                } else {
                    log.warn("Didn't get any offer coverage data from DB.");
                }
            } catch (Exception e) {
                log.error("Error getting offer coverage data from DB: {}", e.getMessage(), e);
            }

            // Cascade count data
            try {
                log.info("Getting cascade count data...");
                cascadeCountData = dbAdapter.selectCascadeCountData();
                if (!cascadeCountData.isEmpty()) {
                    log.info("Successfully received cascade count data ({} records).", cascadeCountData.size());
                    for (CascadeCountQueryResult row : cascadeCountData) {
                        log.debug("Message desc: {}, GZ count: {}, 366 count: {}, total: {}",
                                row.getMessageDesc(),
                                row.getGzCount(),
                                row.getAptekaCount(),
                                row.getTotal()
                        );
                    }
                } else {
                    log.warn("Didn't get any cascade count data from DB.");
                }
            } catch (Exception e) {
                log.error("Error getting cascade count data from DB: {}", e.getMessage(), e);
            }

            // Building email content and sending it to recipients
            log.info("Building email content...");
            String emailContent = buildEmailContent(availableClientBaseDate, communicationCoverageData, offerCoverageData, cascadeCountData);
            log.debug("Email content: {}", emailContent);

            log.info("Getting recipient list from table \"{}\"...", MONITORING_RECIPIENTS);
            List<String> recipients = dbAdapter.getRecipients()
                    .stream()
                    .map(row -> (String) row.get("EMAIL"))
                    .toList();
            log.debug("Received {} recipients from DB.", recipients.size());

            if (!recipients.isEmpty()) {
                String recipientsList = recipients.stream()
                        .map(email -> "\"" + email + "\"")
                        .collect(Collectors.joining(", "));
                log.debug("Full recipients list: [{}]", recipientsList);

                log.info("Starting sending emails to recipients...");
                for (String email : recipients) {
                    try {
                        log.debug("Sending coverage monitoring to recipient with email: {}", email);
                        emailService.sendEmail(email, emailContent);
                        log.debug("Coverage monitoring to recipient with mail \"{}\" has been successfully sent.", email);
                    } catch (Exception e) {
                        log.error("Failed to send email to {}: {}", email, e.getMessage(), e);
                        try {
                            log.info("Sending error email to recipient with mail: {}", email);
                            emailService.sendErrorEmail(email);
                        } catch (Exception mail) {
                            log.error("Failed to send error email to {}: {}", email, mail.getMessage(), mail);
                        }
                    }
                }
                log.info("Finished sending emails to recipients.");
            } else {
                log.warn("Recipient list is empty, emails won't be sent.");
            }
        } catch (Exception e) {
            log.error("An unexpected error occurred during monitoring process: {}", e.getMessage(), e);
        }
    }

    public String buildEmailContent(List<AvailableClientBaseQueryResult> availableClientBaseData,
                                    List<CommunicationCoverageQueryResult> communicationCoverageData,
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
                .append("<p>В таблицах ниже представлены данные по покрытию за текущий месяц.</p>")
                .append("<p>Доступные клиенты: Клиенты, не входящие в ГКГ.</p>")
                .append("<p>Покрытие = Количество уникальных клиентов c коммуникацией / Доступность соответствующего канала.</p>");

        message.append(availableClientBaseTableContent(availableClientBaseData));
        message.append(communicationCoverageTableContent(communicationCoverageData));
        message.append(offerCoverageTableContent(offerCoverageData));
        message.append(cascadeCountTableContent(cascadeCountData));

        message.append("</body></html>");
        return message.toString();
    }

    private String availableClientBaseTableContent(List<AvailableClientBaseQueryResult> availableClientBaseData) {
        String tableHeader = "Доступная база";
        String[] headers = {
                "Бренд",
                "Всего клиентов",
                "Доступные клиенты",
                "Доступен EMAIL",
                "Доступен SMS",
                "Доступен EMAIL или SMS",
                "Доля EMAIL от доступных",
                "Доля SMS от доступных",
                "Доля EMAIL или SMS от доступных"
        };
        StringBuilder tableContent = new StringBuilder();
        int displayedColumnCount = headers.length;

        tableContent.append("<table border='1'>");
        tableContent.append("<tr>").append("<th colspan='").append(displayedColumnCount).append("' class=\"main\">").append(tableHeader).append("</th>").append("</tr>");

        if (availableClientBaseData == null || availableClientBaseData.isEmpty()) {
            tableContent.append("<tr><td colspan='").append(displayedColumnCount).append("'>Нет данных для отображения</td></tr>");
        } else {
            tableContent.append("<tr>");
            for (String header : headers) {
                tableContent.append("<th>").append(header).append("</th>");
            }
            tableContent.append("</tr>");

            for (AvailableClientBaseQueryResult row : availableClientBaseData) {
                tableContent.append("<tr>")
                        .append("<td>").append(row.getBrand()).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getTotalClientsCount())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getAvailableClientCount())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getEmailCount())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getSmsCount())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getEmailAndSmsCount())).append("</td>")
                        .append("<td>").append(row.getEmailPercentage()).append("</td>")
                        .append("<td>").append(row.getSmsPercentage()).append("</td>")
                        .append("<td>").append(row.getEmailAndSmsPercentage()).append("</td>")
                        .append("</tr>");
            }
        }
        tableContent.append("</table>");
        return tableContent.toString();
    }

    private String communicationCoverageTableContent(List<CommunicationCoverageQueryResult> communicationCoverageData) {
        String tableHeader = "Покрытие коммуникациями";
        String[] headers = {
                "Бренд",
                "Тип коммуникации",
                "Канал коммуникации",
                "Количество клиентов c коммуникацией",
                "Количество уникальных клиентов c коммуникацией",
                "Количество коммуникаций на клиента",
                "Покрытие"
        };
        StringBuilder tableContent = new StringBuilder();
        int displayedColumnCount = headers.length;

        tableContent.append("<table border='1'>");
        tableContent.append("<tr>").append("<th colspan='").append(displayedColumnCount).append("' class=\"main\">").append(tableHeader).append("</th>").append("</tr>");

        if (communicationCoverageData == null || communicationCoverageData.isEmpty()) {
            tableContent.append("<tr><td colspan='").append(displayedColumnCount).append("'>Нет данных для отображения</td></tr>");
        } else {
            tableContent.append("<tr>");
            for (String header : headers) {
                tableContent.append("<th>").append(header).append("</th>");
            }
            tableContent.append("</tr>");

            for (CommunicationCoverageQueryResult row : communicationCoverageData) {
                tableContent.append("<tr>")
                        .append("<td>").append(escapeHtml(row.getBrand())).append("</td>")
                        .append("<td>").append(escapeHtml(row.getType())).append("</td>")
                        .append("<td>").append(escapeHtml(row.getChannel())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getCommCount())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getUniqueClientCount())).append("</td>")
                        .append("<td class=\"amount\">").append(row.getCommunicationsPerClient()).append("</td>")
                        .append("<td>").append(escapeHtml(row.getCoverage())).append("</td>")
                        .append("</tr>");
            }
        }
        tableContent.append("</table>");
        return tableContent.toString();
    }

    private String offerCoverageTableContent(List<OfferCoverageQueryResult> offerCoverageData) {
        String tableHeader = "Покрытие офферами";
        String[] headers = {
                "Бренд",
                "Доступная база",
                "Количество офферов",
                "Количество уникальных клиентов с оффером",
                "Количество офферов с коммуникацией",
                "Количество уникальных клиентов с оффером и коммуникацией",
                "Покрытие по офферам",
                "Покрытие по офферам с коммуникацией"
        };
        StringBuilder tableContent = new StringBuilder();
        int displayedColumnCount = headers.length;
        tableContent.append("<table border='1'>");
        tableContent.append("<tr>").append("<th colspan='").append(displayedColumnCount).append("' class=\"main\">").append(tableHeader).append("</th>").append("</tr>");
        if (offerCoverageData == null || offerCoverageData.isEmpty()) {
            tableContent.append("<tr><td colspan='").append(displayedColumnCount).append("'>Нет данных для отображения</td></tr>");
        } else {
            tableContent.append("<tr>");
            for (String header : headers) {
                tableContent.append("<th>").append(header).append("</th>");
            }
            tableContent.append("</tr>");
            for (OfferCoverageQueryResult row : offerCoverageData) {
                tableContent.append("<tr>")
                        .append("<td>").append(escapeHtml(row.getBrand())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getAvailableBase())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getOfferCount())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getUniqueClientsWithOffer())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getOffersWithComm())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getUniqueClientsWithOfferAndComm())).append("</td>")
                        .append("<td>").append(escapeHtml(row.getOfferCoverage())).append("</td>")
                        .append("<td>").append(escapeHtml(row.getOfferWithCommCoverage())).append("</td>")
                        .append("</tr>");
            }
        }
        tableContent.append("</table>");
        return tableContent.toString();
    }

    private String cascadeCountTableContent(List<CascadeCountQueryResult> cascadeCountData) {
        String tableHeader = "Количество отправленных СМС/ПУШ";
        String[] headers = {
                "Метрика",
                "ГОРЗДРАВ",
                "366",
                "Всего"
        };
        StringBuilder tableContent = new StringBuilder();
        int displayedColumnCount = headers.length;
        tableContent.append("<table border='1'>");
        tableContent.append("<tr>").append("<th colspan='").append(displayedColumnCount).append("' class=\"main\">").append(tableHeader).append("</th>").append("</tr>");

        if (cascadeCountData == null || cascadeCountData.isEmpty()) {
            tableContent.append("<tr><td colspan='").append(displayedColumnCount).append("'>Нет данных для отображения</td></tr>");
        } else {
            tableContent.append("<tr>");
            for (String header : headers) {
                tableContent.append("<th>").append(header).append("</th>");
            }
            tableContent.append("</tr>");

            for (CascadeCountQueryResult row : cascadeCountData) {
                tableContent.append("<tr>")
                        .append("<td>").append(escapeHtml(row.getMessageDesc())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getGzCount())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getAptekaCount())).append("</td>")
                        .append("<td class=\"amount\">").append(formatAmount(row.getTotal())).append("</td>")
                        .append("</tr>");
            }
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
            log.error("Error formatting amount: {}", amount, e);
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