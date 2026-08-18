package com.estatelink.notification.eventConsumer;


import com.estatelink.notification.events.InspectionRequestedEvent;
import com.estatelink.notification.events.ListingApprovedEvent;
import com.estatelink.notification.events.UserRegisteredEvent;
import com.estatelink.notification.service.EmailSenderService;
import com.estatelink.notification.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumer {

    private final EmailSenderService emailSenderService;
    private final UserServiceClient userServiceClient;

    @KafkaListener(
            topics = "user.registered",
            groupId = "email-group"
    )
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for: {}", event.getEmail());
        emailSenderService.sendVerificationEmail(event);
    }

    @KafkaListener(
            topics = "inspection.requested",
            groupId = "inspection-email-group",
            containerFactory = "inspectionKafkaListenerContainerFactory"
    )
    public void handleInspectionRequested(InspectionRequestedEvent event) {
        log.info("Received InspectionRequestedEvent for listing {} at slot {} (request {})",
                event.getListingId(), event.getSlotId(), event.getRequestId());

        UserServiceClient.UserRef agent = userServiceClient.findById(event.getAgentId());
        UserServiceClient.UserRef applicant = userServiceClient.findById(event.getApplicantId());

        emailSenderService.sendInspectionRequestedEmail(
                agent != null ? agent.email() : null,
                agent != null ? agent.name() : null,
                applicant != null ? applicant.name() : null,
                event);

        emailSenderService.sendInspectionConfirmationEmail(
                applicant != null ? applicant.email() : null,
                applicant != null ? applicant.name() : null,
                event);
    }

    @KafkaListener(
            topics = "listing.approved",
            groupId = "listing-email-group",
            containerFactory = "listingApprovedKafkaListenerContainerFactory"
    )
    public void handleListingApproved(ListingApprovedEvent event) {
        log.info("Received ListingApprovedEvent for listing {}", event.getListingId());

        String ownerEmail = event.getOwnerEmail();
        if (ownerEmail == null || ownerEmail.isBlank()) {
            UserServiceClient.UserRef owner = userServiceClient.findById(event.getOwnerId());
            ownerEmail = owner != null ? owner.email() : null;
        }

        emailSenderService.sendListingApprovedEmail(ownerEmail, event.getListingTitle());
    }
}
