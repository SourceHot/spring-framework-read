package org.sourcehot.spring.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private JavaMailSender javaMailSender;

    private SimpleMailMessage simpleMailMessage;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("mail/Spring-mail.xml");
        context.close();

    }

    public void sendMailSimple(String to, String subject, String content) throws Exception {

        try {
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(content);

            LOGGER.info("---------------------------{}", simpleMailMessage);
            javaMailSender.send(simpleMailMessage);

        } catch (Exception e) {
            throw new MessagingException("failed to send mail!", e);
        }
    }

    public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void setSimpleMailMessage(SimpleMailMessage simpleMailMessage) {
        this.simpleMailMessage = simpleMailMessage;
    }
}