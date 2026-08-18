package com.estatelink.notification.service;

import com.estatelink.notification.events.InspectionRequestedEvent;
import com.estatelink.notification.events.UserRegisteredEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private static final DateTimeFormatter SLOT_TIME =
            DateTimeFormatter.ofPattern("EEE, MMM d yyyy 'at' h:mm a");

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendVerificationEmail(UserRegisteredEvent event) {
        String verifyLink = baseUrl + "/?token="
                + event.getVerificationToken();
        send(event.getEmail(),
                "Verify your EstateLink account",
                """
                <h2 style="color: #0d9488;">Welcome to EstateLink, %s!</h2>
                <p style="font-size: 16px; line-height: 1.6;">
                  You're one step away from accessing your account.
                  Click the button below to verify your email address.
                </p>
                <a href="%s" style="display:inline-block; background:#0d9488; color:white;
                   padding:12px 28px; text-decoration:none; border-radius:8px;
                   font-size:15px; margin:16px 0;">Verify Email</a>
                <p style="margin-top:24px; font-size:13px; color:#6b7280;">
                  This link expires in 24 hours. If you didn't create an account,
                  you can safely ignore this email.
                </p>
                """.formatted(displayName(event.getName()), verifyLink));
    }

    public void sendListingApprovedEmail(String ownerEmail, String listingTitle) {
        String listingUrl = baseUrl + "/listings";
        send(ownerEmail,
                "Your listing is live on EstateLink",
                """
                <h2 style="color:#0d9488;">Great news, your listing is live!</h2>
                <p style="font-size:16px; line-height:1.6;">
                  "<strong>%s</strong>" has been approved and is now visible to
                  applicants on EstateLink.
                </p>
                <a href="%s" style="display:inline-block; background:#0d9488; color:white;
                   padding:12px 28px; text-decoration:none; border-radius:8px;
                   font-size:15px; margin:16px 0;">View Listings</a>
                """.formatted(escape(listingTitle), listingUrl));
    }

    public void sendInspectionRequestedEmail(String agentEmail, String agentName,
                                             String applicantName,
                                             InspectionRequestedEvent event) {
        String start = event.getSlotStart() != null
                ? SLOT_TIME.format(event.getSlotStart()) : "scheduled time";
        send(agentEmail,
                "New inspection request for your listing",
                """
                <h2 style="color:#0d9488;">New inspection request</h2>
                <p style="font-size:16px; line-height:1.6;">
                  <strong>%s</strong> has requested to inspect your listing
                  (listing ID %s) on <strong>%s</strong>.
                </p>
                <p style="font-size:14px; color:#4b5563;">
                  Applicant message: "%s"
                </p>
                <p style="margin-top:24px; font-size:13px; color:#6b7280;">
                  Log in to your EstateLink dashboard to accept or decline this request.
                </p>
                """.formatted(
                        applicantName != null ? applicantName : "An applicant",
                        event.getListingId(),
                        start,
                        escape(event.getMessage())));
    }

    public void sendInspectionConfirmationEmail(String applicantEmail, String applicantName,
                                                InspectionRequestedEvent event) {
        String start = event.getSlotStart() != null
                ? SLOT_TIME.format(event.getSlotStart()) : "scheduled time";
        send(applicantEmail,
                "Inspection request received",
                """
                <h2 style="color:#0d9488;">Inspection request received</h2>
                <p style="font-size:16px; line-height:1.6;">
                  Hi %s, your request to inspect listing <strong>%s</strong> on
                  <strong>%s</strong> has been received. The agent will review it
                  and respond shortly.
                </p>
                """.formatted(
                        applicantName != null ? applicantName : "there",
                        event.getListingId(),
                        start));
    }

    private void send(String to, String subject, String html) {
        if (to == null || to.isBlank()) {
            log.warn("Skipping email '{}': no recipient address", subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(wrap(html), true);
            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);
        } catch (MessagingException e) {
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }

    private String wrap(String body) {
        return """
            <html>
              <body style="font-family: Arial, sans-serif; padding: 32px; color: #111;">
                %s
              </body>
            </html>
            """.formatted(body);
    }

    private String displayName(String name) {
        return (name != null && !name.isBlank()) ? name : "there";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
