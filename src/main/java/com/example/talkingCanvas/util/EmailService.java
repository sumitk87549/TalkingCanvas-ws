package com.example.talkingCanvas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${admin.default.email}")
    private String adminEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Talking Canvas!");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                    "Welcome to Talking Canvas! We're excited to have you join our community of art enthusiasts.\n\n" +
                    "Explore our exquisite collection of oil paintings and find the perfect piece for your space.\n\n" +
                    "If you have any questions, feel free to contact us.\n\n" +
                    "Best regards,\n" +
                    "The Talking Canvas Team",
                    userName
            ));
            mailSender.send(message);
            logger.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    public void sendOrderConfirmationEmail(String toEmail, String userName, String orderNumber, String totalAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - " + orderNumber);
            message.setText(String.format(
                    "Dear %s,\n\n" +
                    "Thank you for your order!\n\n" +
                    "Order Number: %s\n" +
                    "Total Amount: %s\n\n" +
                    "We will process your order shortly and contact you for COD payment upon delivery.\n\n" +
                    "Best regards,\n" +
                    "The Talking Canvas Team",
                    userName, orderNumber, totalAmount
            ));
            mailSender.send(message);
            logger.info("Order confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email to: {}", toEmail, e);
        }
    }

    public void sendContactFormNotification(String name, String email, String phone, String subject, String messageContent) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("New Contact Form Submission: " + subject);
            message.setText(String.format(
                    "New contact form submission:\n\n" +
                    "Name: %s\n" +
                    "Email: %s\n" +
                    "Phone: %s\n" +
                    "Subject: %s\n\n" +
                    "Message:\n%s",
                    name, email, phone != null ? phone : "N/A", subject, messageContent
            ));
            mailSender.send(message);
            logger.info("Contact form notification sent to admin");
        } catch (Exception e) {
            logger.error("Failed to send contact form notification", e);
        }
    }
}
