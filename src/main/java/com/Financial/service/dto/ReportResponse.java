package com.Financial.service.dto;


import com.Financial.service.dto.FinancialRecordResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {


    private String reportGeneratedAt;     
    private String appliedDateRange;      
    private String appliedUserId;         
    private String appliedType;          
    private String appliedCategory;     

    
    private long totalRecords;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;

  
    private Map<String, BigDecimal> categoryWiseTotals;

    private Map<String, BigDecimal> typeWiseTotals;

    
    private Map<String, Map<String, BigDecimal>> typeAndCategoryBreakdown;

  
    private Map<String, UserReportSummary> perUserSummary;

   
    private Map<String, BigDecimal> monthlyTrends;

   
    private Map<String, BigDecimal> weeklyTrends;

    
    private List<FinancialRecordResponse> records;

    @JsonIgnore  // JSON response mein nahi aayega
    private CompletableFuture<byte[]> pdfFuture;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserReportSummary {
        private String userId;
        private String userName;
        private long totalRecords;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netBalance;
        private Map<String, BigDecimal> categoryWiseTotals;
    }

}