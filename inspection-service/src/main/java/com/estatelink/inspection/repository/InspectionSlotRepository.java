package com.estatelink.inspection.repository;

import com.estatelink.inspection.domain.InspectionSlot;
import com.estatelink.inspection.domain.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InspectionSlotRepository extends JpaRepository<InspectionSlot, UUID> {

    List<InspectionSlot> findByListingId(UUID listingId);

    List<InspectionSlot> findByListingIdAndStatus(UUID listingId, SlotStatus status);

    List<InspectionSlot> findByAgentId(UUID agentId);

    Optional<InspectionSlot> findByIdAndAgentId(UUID id, UUID agentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from InspectionSlot s where s.id = :id")
    Optional<InspectionSlot> findByIdForUpdate(@Param("id") UUID id);

    /**
     * Finds any non-cancelled slot of a listing whose time range overlaps
     * [start, end]. Two ranges overlap when a.start < b.end AND a.end > b.start.
     */
    @Query("select s from InspectionSlot s " +
           "where s.listingId = :listingId " +
           "and s.status <> com.estatelink.inspection.domain.SlotStatus.CANCELLED " +
           "and s.slotStart < :end and s.slotEnd > :start")
    List<InspectionSlot> findOverlapping(@Param("listingId") UUID listingId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}
