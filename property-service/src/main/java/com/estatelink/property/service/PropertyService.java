package com.estatelink.property.service;

import com.estatelink.property.domain.AvailabilityStatus;
import com.estatelink.property.domain.Property;
import com.estatelink.property.dto.requests.CreatePropertyRequest;
import com.estatelink.property.dto.responses.PropertyResponse;
import com.estatelink.property.dto.responses.geocoding.GeocodingResult;
import com.estatelink.property.exception.PropertyNotFoundException;
import com.estatelink.property.exception.UnauthorizedException;
import com.estatelink.property.repository.PropertyRepository;
import com.estatelink.property.utils.PropertyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final GeocodingService geocodingService;
    private final PropertyMapper propertyMapper;


    @Transactional
    public PropertyResponse createProperty(CreatePropertyRequest request, UUID ownerId){

        Property property = Property.builder()
                .ownerId(ownerId)
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .propertyType(request.getPropertyType())
                .price(request.getPrice())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .squareFootage(request.getSquareFootage())
                .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>())
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .addressVerified(false)
                .build();
        try{
            GeocodingResult geocodingResult = geocodingService.geocode(request.getAddress(), request.getCity(), request.getState());

            property.setLatitude(geocodingResult.getLatitude());
            property.setLongitude(geocodingResult.getLongitude());
            property.setFormattedAddress(geocodingResult.getFormattedAddress());
            property.setAddressVerified(true);
        }catch (Exception e){
            log.warn("Geocoding unavailable, saving without coordinates: {}", e.getMessage());
        }

        return propertyMapper.toResponse(propertyRepository.save(property));
    }


    public PropertyResponse getProperty(UUID id){
        return propertyMapper.toResponse(findById(id));
    }


    public List<PropertyResponse> getMyProperties(UUID ownerId){
        return propertyRepository.findByOwnerId(ownerId)
                .stream().map(propertyMapper::toResponse).toList();
    }


    @Transactional
    public PropertyResponse assignAgent(UUID propertyId, UUID agentId, UUID requesterId) {
        Property property = findById(propertyId);

        if (!property.getOwnerId().equals(requesterId)) {
            throw new UnauthorizedException("Only the property owner can assign an agent");
        }

        property.setAgentId(agentId);
        return propertyMapper.toResponse(propertyRepository.save(property));
    }

    @Transactional
    public PropertyResponse updateStatus(UUID propertyId, AvailabilityStatus availabilityStatus, UUID requesterId) {
        Property property = findById(propertyId);

        if (!property.getOwnerId().equals(requesterId)) {
            throw new UnauthorizedException("Only the property owner can update status");
        }

        property.setAvailabilityStatus(availabilityStatus);
        return propertyMapper.toResponse(propertyRepository.save(property));
    }

    @Transactional
    public PropertyResponse updateImages(UUID propertyId, List<String> imageUrls, UUID requesterId, String requesterRole) {
        Property property = findById(propertyId);

        boolean isAdmin = "ADMIN".equals(requesterRole);
        boolean isOwner = property.getOwnerId().equals(requesterId);
        boolean isAssignedAgent = property.getAgentId() != null && property.getAgentId().equals(requesterId);
        if (!isAdmin && !isOwner && !isAssignedAgent) {
            throw new UnauthorizedException("Only the owner, assigned agent, or an admin can update photos");
        }

        property.setImageUrls(imageUrls != null ? imageUrls : new ArrayList<>());
        return propertyMapper.toResponse(propertyRepository.save(property));
    }

    private Property findById(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found: " + id));
    }
}