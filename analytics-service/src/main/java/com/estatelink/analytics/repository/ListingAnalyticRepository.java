package com.estatelink.analytics.repository;

import com.estatelink.analytics.domain.ListingAnalytic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingAnalyticRepository extends JpaRepository<ListingAnalytic, UUID> {

    List<ListingAnalytic> findAllByStatus(String status);
}
