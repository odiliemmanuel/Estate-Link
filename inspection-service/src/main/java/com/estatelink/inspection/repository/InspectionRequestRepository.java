package com.estatelink.inspection.repository;

import com.estatelink.inspection.domain.InspectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InspectionRequestRepository extends JpaRepository<InspectionRequest, UUID> {

    List<InspectionRequest> findByApplicantId(UUID applicantId);

    List<InspectionRequest> findByAgentId(UUID agentId);

    List<InspectionRequest> findBySlotIdIn(List<UUID> slotIds);

    Optional<InspectionRequest> findBySlotId(UUID slotId);

    Optional<InspectionRequest> findByIdAndApplicantId(UUID id, UUID applicantId);

    Optional<InspectionRequest> findByIdAndAgentId(UUID id, UUID agentId);
}
