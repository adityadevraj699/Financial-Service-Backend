package com.Financial.service.dto;

import com.Financial.service.entity.Category;
import com.Financial.service.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FinancialRecordRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount format invalid (max 13 digits, 2 decimal)")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;               

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDateTime date;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @NotBlank(message = "User ID is required")
	private String userId;
    
    
    

	                      
}