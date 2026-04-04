package com.Financial.service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    
    private BigDecimal totalIncome;      
    private BigDecimal totalExpense;      
    private BigDecimal netBalance;       

   
    private Map<String, BigDecimal> categoryWiseTotals;

   
    private Map<String, Map<String, BigDecimal>> typeAndCategoryWiseTotals;

  
    private Map<String, BigDecimal> monthlyTrends;

   
    private Map<String, BigDecimal> weeklyTrends;

   
    private List<FinancialRecordResponse> recentActivities;

   
    private long totalRecords;

   
    private String appliedDateRange;
}