package com.Financial.service.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialFilterRequest {

    private String userId;
    private String type;
    private String category;

    @Min(value = 1, message = "Days must be at least 1")
    @Max(value = 365, message = "Days cannot exceed 365")
    private Integer days;

    
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$",
        message = "Invalid 'from' date format. Use: yyyy-MM-ddTHH:mm:ss"
    )
    private String from;

    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$",
        message = "Invalid 'to' date format. Use: yyyy-MM-ddTHH:mm:ss"
    )
    private String to;

   
    @Pattern(
        regexp = "^\\d+(\\.\\d{1,2})?$",
        message = "Invalid minAmount. Use numeric value like 100 or 100.50"
    )
    private String minAmount;

    @Pattern(
        regexp = "^\\d+(\\.\\d{1,2})?$",
        message = "Invalid maxAmount. Use numeric value like 5000 or 5000.99"
    )
    private String maxAmount;
    
    
     
    
    @AssertTrue(message = "'from' date must be before or equal to 'to' date")
    public boolean isFromBeforeTo() {
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            return true;
        }
        try {
            return !LocalDateTime.parse(from).isAfter(LocalDateTime.parse(to));
        } catch (Exception e) {
            return true;
        }
    }

    @AssertTrue(message = "'minAmount' must be less than or equal to 'maxAmount'")
    public boolean isMinLessThanMax() {
        if (minAmount == null || minAmount.isBlank() || maxAmount == null || maxAmount.isBlank()) {
            return true;
        }
        try {
            return new BigDecimal(minAmount).compareTo(new BigDecimal(maxAmount)) <= 0;
        } catch (Exception e) {
            return true;
        }
    }
}