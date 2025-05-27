package rubbles.monitoring.commcoverage.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(userName));

            InternetAddress[] toAddresses = {new InternetAddress(email)};

            msg.setRecipients(Message.RecipientType.TO, toAddresses);
            msg.setSubject("Отчет по NPS");
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

            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(userName));

            InternetAddress[] toAddresses = {new InternetAddress(email)};

            msg.setRecipients(Message.RecipientType.TO, toAddresses);
            msg.setSubject("ОШИБКА Отчет по NPS");
            msg.setSentDate(new java.util.Date());

            msg.setContent("При отправке отчета по NPS произошла ошибка<br><br>", "text/html; charset=utf-8");

            Transport.send(msg);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
