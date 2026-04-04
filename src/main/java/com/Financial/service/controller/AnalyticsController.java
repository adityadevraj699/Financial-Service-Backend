package com.Financial.service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Financial.service.dto.ApiResponse;
import com.Financial.service.dto.DashboardResponse;
import com.Financial.service.dto.FinancialFilterRequest;
import com.Financial.service.dto.FinancialRecordResponse;
import com.Financial.service.dto.ReportResponse;
import com.Financial.service.dto.UserDto;
import com.Financial.service.service.AdminService;
import com.Financial.service.service.AnalystService;
import com.Financial.service.service.FinancialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analyst/")
public class AnalyticsController {
	
	private final FinancialService financialService;
	private final AnalystService analystservice;
	
	  // get All Users
	  @GetMapping("/users")
	  public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
	    List<UserDto> users = analystservice.getAllUsers();
	    return ResponseEntity
	            .status(HttpStatus.OK)
	            .body(ApiResponse.success("Users retrieved successfully", users));
	 }
	
	  //get all records with or without filter
	   @GetMapping("/records")
	   public ResponseEntity<ApiResponse<List<FinancialRecordResponse>>> getRecordsByFilters(
	            @RequestParam(required = false) String userId,
	            @RequestParam(required = false) String type,
	            @RequestParam(required = false) String category,
	            @RequestParam(required = false) String from,
	            @RequestParam(required = false) String to,
	            @RequestParam(required = false) String min,
	            @RequestParam(required = false) String max) {

	        List<FinancialRecordResponse> records = financialService.getRecordsByFilters(
	                userId, type, category, from, to, min, max);
	        return ResponseEntity
		            .status(HttpStatus.OK)
		            .body(ApiResponse.success("Financial records retrieved successfully", records));
	    }
	
	
	    // View Recent Activities
		@GetMapping("/recent")
		public ResponseEntity<ApiResponse<List<FinancialRecordResponse>>> getRecentActivities(
		        @RequestParam(required = false) String userId,
		        @RequestParam(defaultValue = "7") int days) {  

		    List<FinancialRecordResponse> result = financialService.getRecentActivities(userId, days);
		    return ResponseEntity
		    		.status(HttpStatus.OK)
		    		.body(ApiResponse.success("Recent activities retrieved successfully", result));
		}

	
	    // view Users Dashboards
		@GetMapping("/dashboard")
		public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
		        @Valid @ModelAttribute FinancialFilterRequest filter) {
			DashboardResponse result =financialService.getDashboard(filter);
		    return ResponseEntity
		    		.status(HttpStatus.OK)
		    		.body(ApiResponse.success("User Dashboard Summary", result));
		}
		
		// generate report Summary with filter by date range, category, type and amount range, user id, last date or generate All report
		@GetMapping("/reports")
	    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
	            @Valid @ModelAttribute FinancialFilterRequest filter) {
			ReportResponse report =financialService.generateReport(filter);
			report.setPdfFuture(null);
	        return ResponseEntity
	        		.status(HttpStatus.OK)
	        		.body(ApiResponse.success("Report generated successfully", report));
	    }
		
		
	   
	    @GetMapping("/reports/download")
	    public ResponseEntity<?> downloadPdfReport(
	            @Valid @ModelAttribute FinancialFilterRequest filter) {

	        try {
	            
	            ReportResponse report = financialService.generateReport(filter);

	           
	            byte[] pdfBytes = report.getPdfFuture().get();

	           
	            String fileName = "Financial_Report_" +
	                    java.time.LocalDate.now() + ".pdf";

	            return ResponseEntity.ok()
	                    .header("Content-Disposition",
	                            "attachment; filename=\"" + fileName + "\"")
	                    .header("Content-Type", "application/pdf")
	                    .body(pdfBytes);

	        } catch (Exception e) {
	        	return ResponseEntity
	                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(ApiResponse.error("PDF generation failed: " + e.getMessage())); 
	        }
	    }

}
