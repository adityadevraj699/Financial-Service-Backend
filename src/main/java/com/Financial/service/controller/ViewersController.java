package com.Financial.service.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.Financial.service.service.FinancialService;
import com.Financial.service.service.ViewersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/viewer/")
public class ViewersController {

    private final FinancialService financialService;
    private final ViewersService viewersService;

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<FinancialRecordResponse>>> getRecordsByFilters(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String min,
            @RequestParam(required = false) String max) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getDetails(); 

        List<FinancialRecordResponse> records = financialService.getRecordsByFilters(
                userId, type, category, from, to, min, max);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Financial records retrieved successfully", records));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<FinancialRecordResponse>>> getRecentActivities(
            @RequestParam(defaultValue = "7") int days) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getDetails(); 

        List<FinancialRecordResponse> result = financialService.getRecentActivities(userId, days);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Recent activities retrieved successfully", result));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @Valid @ModelAttribute FinancialFilterRequest filter) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getDetails(); 
        filter.setUserId(userId);

        DashboardResponse result = financialService.getDashboard(filter);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("User Dashboard Summary", result));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @Valid @ModelAttribute FinancialFilterRequest filter) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getDetails(); 
        filter.setUserId(userId);

        ReportResponse report = financialService.generateReport(filter);
        report.setPdfFuture(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Report generated successfully", report));
    }

    @GetMapping("/reports/download")
    public ResponseEntity<?> downloadPdfReport(
            @Valid @ModelAttribute FinancialFilterRequest filter) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (String) auth.getDetails();
            filter.setUserId(userId);

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
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getUserProfile() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userId = (String) auth.getDetails(); 
		UserDto userProfile = viewersService.getViewerById(userId);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(ApiResponse.success("User profile retrieved successfully", userProfile));
	}
}