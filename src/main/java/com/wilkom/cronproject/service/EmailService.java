package com.wilkom.cronproject.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    @Value("${aws.ses.from-email}")
    private String fromEmail;

    @Autowired
    public EmailService(AmazonSimpleEmailService amazonSimpleEmailService) {
        this.amazonSimpleEmailService = amazonSimpleEmailService;
    }

    public void sendEmail(String to, String subject, String htmlBody) {
        sendEmail(List.of(to), subject, htmlBody);
    }

    public void sendEmail(List<String> to,
            String subject, String body) {
        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(to))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(body)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(subject)))
                    .withSource(fromEmail);

            SendEmailResult result = amazonSimpleEmailService.sendEmail(request);
            logger.info("HTML email sent successfully. Message ID: {}", result.getMessageId());
        } catch (MessageRejectedException ex) {
            logger.error("Failed to send HTML email. Error: {}", ex.getMessage(), ex);
            throw new MessageRejectedException("Failed to send HTML email");
        }
    }
}
