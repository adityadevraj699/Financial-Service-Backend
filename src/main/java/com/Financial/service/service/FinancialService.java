package com.Financial.service.service;

import com.Financial.service.dto.DashboardResponse;
import com.Financial.service.dto.FinancialFilterRequest;
import com.Financial.service.dto.FinancialRecordRequest;
import com.Financial.service.dto.FinancialRecordResponse;
import com.Financial.service.dto.ReportResponse;
import com.Financial.service.entity.Category;
import com.Financial.service.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FinancialService {

   
	// create
    FinancialRecordResponse createRecord(FinancialRecordRequest request);   
    
    // update 
    FinancialRecordResponse updateRecord(String recordId, FinancialRecordRequest request); 
    
    // delete
    void deleteRecord(String recordId);   
    
    // get all records by id
    FinancialRecordResponse getRecordById(String recordId);
    
    //get all Records
    List<FinancialRecordResponse> getAllRecords();
    
//    // get records filters by user id
//    List<FinancialRecordResponse> getRecordsByUserId(String userId);
//    
//    // get record filters by types
//    List<FinancialRecordResponse> getRecordsByType(String userId, TransactionType type);
//    
//    // get record filters by category
//    List<FinancialRecordResponse> getRecordsByCategory(String userId, Category category);
//    
//    // get record filters by date range
//    List<FinancialRecordResponse> getRecordsByDateRange(String userId, LocalDateTime from, LocalDateTime to);
//    
//    // get record filters by amount range
//    List<FinancialRecordResponse> getRecordsByAmountRange(String userId, BigDecimal min, BigDecimal max);
    
    // get all records filters by uiser id , type, category, date range and amount range
    List<FinancialRecordResponse> getRecordsByFilters(
            String userId,
            String type,
            String category,
            String from,
            String to,
            String min,
            String max
    );
    
    List<FinancialRecordResponse> getRecentActivities(String userId, int days);
    DashboardResponse getDashboard(FinancialFilterRequest filter);
    ReportResponse generateReport( FinancialFilterRequest filter);
}