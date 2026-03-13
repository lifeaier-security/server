package web.server.api.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSendService {

    private final JavaMailSender mailSender;

    public MailSendService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@www.lifeaier.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
