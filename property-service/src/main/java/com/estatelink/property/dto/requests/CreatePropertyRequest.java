package com.estatelink.property.dto.requests;

import com.estatelink.property.domain.PropertyType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatePropertyRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Bedrooms can't be negative")
    private Integer bedrooms;

    @Min(value = 0, message = "Bathrooms can't be negative")
    private Integer bathrooms;

    @Min(value = 0, message = "Square footage can't be negative")
    private Integer squareFootage;

    private List<String> imageUrls;
}