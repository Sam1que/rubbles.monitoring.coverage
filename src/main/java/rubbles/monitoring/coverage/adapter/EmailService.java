package rubbles.monitoring.coverage.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;

@Service
@Slf4j
public class EmailService {

    @Value("${smtp.host}")
    private String host;

    @Value("${smtp.port}")
    private String port;

    @Value("${smtp.username}")
    private String userName;

    @Value("${smtp.password}")
    private String password;

    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("LLLL", new Locale("ru"));

    public void sendEmail(String email, String htmlBody) throws Exception {
        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    try {
                        return new PasswordAuthentication(userName, password);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            String rawMonthName = LocalDate.now().format(MONTH_FORMATTER);
            String capitalizedMonthName = rawMonthName.substring(0, 1).toUpperCase() + rawMonthName.substring(1);

            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(userName));

            InternetAddress[] toAddresses = {new InternetAddress(email)};

            msg.setRecipients(Message.RecipientType.TO, toAddresses);
            msg.setSubject("Отчет по покрытию за " + capitalizedMonthName);
            msg.setSentDate(new java.util.Date());

            msg.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(msg);
        } catch(Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void sendErrorEmail(String email) throws Exception {
        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    try {
                        return new PasswordAuthentication(userName, password);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            String rawMonthName = LocalDate.now().format(MONTH_FORMATTER);
            String capitalizedMonthName = rawMonthName.substring(0, 1).toUpperCase() + rawMonthName.substring(1);

            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(userName));

            InternetAddress[] toAddresses = {new InternetAddress(email)};

            msg.setRecipients(Message.RecipientType.TO, toAddresses);
            msg.setSubject("ОШИБКА Отчет по покрытию за " + capitalizedMonthName);
            msg.setSentDate(new java.util.Date());

            msg.setContent("При отправке отчета по покрытию произошла ошибка<br><br>", "text/html; charset=utf-8");

            Transport.send(msg);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
