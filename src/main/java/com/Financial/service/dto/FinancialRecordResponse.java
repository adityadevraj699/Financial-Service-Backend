package com.Financial.service.dto;

import com.Financial.service.entity.Category;
import com.Financial.service.entity.FinancialRecord;
import com.Financial.service.entity.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordResponse {

    private String id;
    private BigDecimal amount;
    private TransactionType type;
    private Category category;
    private LocalDateTime date;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;
    private String userName;

    // ✅ Entity → Response DTO
    public static FinancialRecordResponse FinancialRecord(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .notes(record.getNotes())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .userId(record.getUser().getId())
                .userName(record.getUser().getName())
                .build();
    }
}