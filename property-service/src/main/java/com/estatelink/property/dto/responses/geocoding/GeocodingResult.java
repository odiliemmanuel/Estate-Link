package com.estatelink.property.dto.responses.geocoding;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeocodingResult {

    private Double latitude;
    private Double longitude;
    private String formattedAddress;
    private String locationType;  // ROOFTOP, RANGE_INTERPOLATED, APPROXIMATE

}
