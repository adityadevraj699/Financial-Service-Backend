package com.Financial.service.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.DeleteExchange;

import com.Financial.service.config.JwtService;
import com.Financial.service.dto.ApiResponse;
import com.Financial.service.dto.CreateUserRequest;
import com.Financial.service.dto.DashboardResponse;
import com.Financial.service.dto.FinancialFilterRequest;
import com.Financial.service.dto.FinancialRecordRequest;
import com.Financial.service.dto.FinancialRecordResponse;
import com.Financial.service.dto.ReportResponse;
import com.Financial.service.dto.UpdateStatusRequest;
import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.UsersRole;
import com.Financial.service.repository.UserRepository;
import com.Financial.service.service.AdminService;
import com.Financial.service.service.FinancialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
	
	private final AdminService adminService;
	private final FinancialService financialService;
	

	//create users
	@PostMapping("/user")
	public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserDto userResponse = adminService.createUser(request);
	    return ResponseEntity
	            .status(HttpStatus.CREATED)
	            .body(ApiResponse.success("User created successfully", userResponse));
	}	
	
	
	// active & deactive users
	@PatchMapping("/users/{userId}/status")
	public ResponseEntity<ApiResponse<String>> toggleUserStatus(
	        @PathVariable String userId) {

	    String message = adminService.setActiveStatus(userId);

	    return ResponseEntity.ok(ApiResponse.success(message, null));
	}
	
	// update role
	@PatchMapping("/user/{userId}/role")
	public ResponseEntity<ApiResponse<UserDto>> updateUserRole(@PathVariable String userId, @RequestParam String role) {
	    UserDto updatedUser = adminService.updateUserRole(userId, role);
	    return ResponseEntity
	            .status(HttpStatus.OK)
	            .body(ApiResponse.success("User role updated successfully", updatedUser));
	}

	
	// get All Users
	@GetMapping("/users")
	public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers(
	        @RequestParam(required = false) UsersRole role) {

	    List<UserDto> users = adminService.getAllUsers(role);
	    return ResponseEntity
	            .status(HttpStatus.OK)
	            .body(ApiResponse.success("Users retrieved successfully", users));
	}
	
	// create financial record
	@PostMapping("/record")
	public ResponseEntity<ApiResponse<FinancialRecordResponse>> createFinancialRecord(@Valid @RequestBody FinancialRecordRequest request) {
		FinancialRecordResponse recordResponse = financialService.createRecord(request);
	    return ResponseEntity
	            .status(HttpStatus.CREATED)
	            .body(ApiResponse.success("Financial record created successfully", recordResponse));

	}
	// update financial record
	@PutMapping("/record/{recordId}")
	public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateFinancialRecord(@PathVariable String recordId, @Valid @RequestBody FinancialRecordRequest request) {
		FinancialRecordResponse recordResponse = financialService.updateRecord(recordId, request);
	    return ResponseEntity
	            .status(HttpStatus.OK)
	            .body(ApiResponse.success("Financial record updated successfully", recordResponse));
	}
	
	// delete financial record
	@DeleteMapping("/record/{recordId}")
	public ResponseEntity<ApiResponse<Void>> deleteFinancialRecord(
	        @PathVariable String recordId) {

	    financialService.deleteRecord(recordId);

	    return ResponseEntity.ok(
	            ApiResponse.success("Financial record deleted successfully", null)
	    );
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
