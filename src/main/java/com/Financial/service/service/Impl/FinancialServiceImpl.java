package com.Financial.service.service.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.Financial.service.dto.DashboardResponse;
import com.Financial.service.dto.FinancialFilterRequest;
import com.Financial.service.dto.FinancialRecordRequest;
import com.Financial.service.dto.FinancialRecordResponse;
import com.Financial.service.dto.ReportResponse;
import com.Financial.service.entity.Category;
import com.Financial.service.entity.FinancialRecord;
import com.Financial.service.entity.TransactionType;
import com.Financial.service.entity.Users;
import com.Financial.service.exception.FinancialRecordExpection;
import com.Financial.service.exception.UserNotFoundException;
import com.Financial.service.repository.FinancialRecordRepository;
import com.Financial.service.repository.UserRepository;
import com.Financial.service.service.FinancialService;
import com.Financial.service.utils.PdfReportUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinancialServiceImpl implements FinancialService {
	
	private final FinancialRecordRepository financialRecordRepository;
	private final UserRepository userRepository;
	private final PdfReportUtil pdfReportUtil;

	@Override
	public FinancialRecordResponse createRecord(FinancialRecordRequest request) {
		Users user = userRepository.findByIdAndActiveTrue(request.getUserId())
				.orElseThrow(() -> new UserNotFoundException(request.getUserId(), "Cannot create financial record. User not found."));
		
		var record =FinancialRecord.builder()
				.user(user)
				.amount(request.getAmount())
				.type(request.getType())
				.category(request.getCategory())
				.date(request.getDate())
				.notes(request.getNotes())
				.active(true)
				.build();
		
		FinancialRecord savedRecord = financialRecordRepository.save(record);
		
		return FinancialRecordResponse.FinancialRecord(savedRecord);
	}

	@Transactional
	@Override
	public FinancialRecordResponse updateRecord(String recordId, FinancialRecordRequest request) {

	    Users user = userRepository.findByIdAndActiveTrue(request.getUserId())
	            .orElseThrow(() -> new UserNotFoundException(request.getUserId(), "User not found"));

	    FinancialRecord record = financialRecordRepository.findById(recordId)
	            .orElseThrow(() -> new FinancialRecordExpection("Financial record not found with id: " + recordId));

	    record.setUser(user);
	    record.setAmount(request.getAmount());
	    record.setType(request.getType());
	    record.setCategory(request.getCategory());
	    record.setDate(request.getDate());
	    record.setNotes(request.getNotes());

	    return FinancialRecordResponse.FinancialRecord(record);
	}

	@Transactional
	@Override
	public void deleteRecord(String recordId) {

	    FinancialRecord record = financialRecordRepository.findById(recordId)
	            .orElseThrow(() -> new FinancialRecordExpection(
	                    "Financial record not found with id: " + recordId));

	    record.setActive(false);   
	}
	
	

	@Override
	public FinancialRecordResponse getRecordById(String recordId) {
		FinancialRecord record = financialRecordRepository.findById(recordId)
				 .orElseThrow(() -> new FinancialRecordExpection("Financial record not found with id: " + recordId));
		
		 return FinancialRecordResponse.FinancialRecord(record);
	}
	
	

	@Override
	public List<FinancialRecordResponse> getAllRecords() {
		return financialRecordRepository.findByActiveTrue().stream()
				.map(FinancialRecordResponse::FinancialRecord)
				.toList();
	}



	@Override
    public List<FinancialRecordResponse> getRecordsByFilters(
            String userId,
            String type,
            String category,
            String from,
            String to,
            String min,
            String max) {

        
        List<FinancialRecord> records;

        if (userId == null || userId.isBlank()) {
            records = financialRecordRepository.findByActiveTrue();
        } else {
            userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId, "User not found"));
            records = financialRecordRepository.findByUserIdAndActiveTrue(userId);
        }

        
        TransactionType transactionType = null;
        if (type != null && !type.isBlank()) {
            try {
                transactionType = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction type: '" + type + "'. Valid values: INCOME, EXPENSE");
            }
        }

        Category cat = null;
        if (category != null && !category.isBlank()) {
            try {
                cat = Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid category: '" + category + "'");
            }
        }

       
        LocalDateTime fromDate = null;
        if (from != null && !from.isBlank()) {
            try {
                fromDate = LocalDateTime.parse(from); 
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid 'from' date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
            }
        }

        LocalDateTime toDate = null;
        if (to != null && !to.isBlank()) {
            try {
                toDate = LocalDateTime.parse(to);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid 'to' date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
            }
        }

        
        BigDecimal minAmount = null;
        if (min != null && !min.isBlank()) {
            try {
                minAmount = new BigDecimal(min);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid 'min' amount: '" + min + "'");
            }
        }

        BigDecimal maxAmount = null;
        if (max != null && !max.isBlank()) {
            try {
                maxAmount = new BigDecimal(max);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid 'max' amount: '" + max + "'");
            }
        }

        
        final TransactionType finalType = transactionType;
        final Category finalCat = cat;
        final LocalDateTime finalFrom = fromDate;
        final LocalDateTime finalTo = toDate;
        final BigDecimal finalMin = minAmount;
        final BigDecimal finalMax = maxAmount;

        
        return records.stream()
                .filter(r -> finalType == null || r.getType() == finalType)
                .filter(r -> finalCat == null || r.getCategory() == finalCat)
                .filter(r -> finalFrom == null || !r.getDate().isBefore(finalFrom))  
                .filter(r -> finalTo == null || !r.getDate().isAfter(finalTo))      
                .filter(r -> finalMin == null || r.getAmount().compareTo(finalMin) >= 0)
                .filter(r -> finalMax == null || r.getAmount().compareTo(finalMax) <= 0)
                .map(FinancialRecordResponse::FinancialRecord)
                .toList();
    }
	
	
	@Override
	public List<FinancialRecordResponse> getRecentActivities(String userId, int days) {

	
	    if (days <= 0) {
	        throw new IllegalArgumentException("Days must be greater than 0");
	    }

	 
	    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

	   
	    List<FinancialRecord> records;

	    if (userId == null || userId.isBlank()) {
	        records = financialRecordRepository.findByActiveTrue();
	    } else {
	        userRepository.findById(userId)
	                .orElseThrow(() -> new UserNotFoundException(userId, "User not found"));
	        records = financialRecordRepository.findByUserIdAndActiveTrue(userId);
	    }

	  
	    return records.stream()
	            .filter(r -> r.getDate().isAfter(cutoffDate))
	            .sorted(Comparator.comparing(FinancialRecord::getDate).reversed()) 
	            .map(FinancialRecordResponse::FinancialRecord)
	            .toList();
	}
	
	 @Override
	    public DashboardResponse getDashboard(FinancialFilterRequest filter) {


	        List<FinancialRecord> records;

	        if (filter.getUserId() == null || filter.getUserId().isBlank()) {

	            records = financialRecordRepository.findByActiveTrue();
	        } else {
	          
	            userRepository.findById(filter.getUserId())
	                    .orElseThrow(() -> new UserNotFoundException(
	                            filter.getUserId(), "User not found"));
	            records = financialRecordRepository
	                    .findByUserIdAndActiveTrue(filter.getUserId());
	        }


	        LocalDateTime fromDate = null;
	        LocalDateTime toDate = null;
	        String appliedDateRange = "All Time";

	        if (filter.getFrom() != null && !filter.getFrom().isBlank()) {
	           
	            try {
	                fromDate = LocalDateTime.parse(filter.getFrom());
	                toDate = (filter.getTo() != null && !filter.getTo().isBlank())
	                        ? LocalDateTime.parse(filter.getTo())
	                        : LocalDateTime.now();
	                appliedDateRange = filter.getFrom() + " to " + filter.getTo();
	            } catch (Exception e) {
	                throw new IllegalArgumentException(
	                        "Invalid date format. Use: yyyy-MM-ddTHH:mm:ss");
	            }
	        } else if (filter.getDays() != null && filter.getDays() > 0) {
	         
	            fromDate = LocalDateTime.now().minusDays(filter.getDays());
	            toDate = LocalDateTime.now();
	            appliedDateRange = "Last " + filter.getDays() + " days";
	        }


	        TransactionType transactionType = null;
	        if (filter.getType() != null && !filter.getType().isBlank()) {
	            try {
	                transactionType = TransactionType.valueOf(
	                        filter.getType().toUpperCase());
	            } catch (IllegalArgumentException e) {
	                throw new IllegalArgumentException(
	                        "Invalid type: '" + filter.getType() + "'. Use INCOME or EXPENSE");
	            }
	        }

	        Category category = null;
	        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
	            try {
	                category = Category.valueOf(filter.getCategory().toUpperCase());
	            } catch (IllegalArgumentException e) {
	                throw new IllegalArgumentException(
	                        "Invalid category: '" + filter.getCategory() + "'");
	            }
	        }


	        BigDecimal minAmount = null;
	        BigDecimal maxAmount = null;

	        if (filter.getMinAmount() != null && !filter.getMinAmount().isBlank()) {
	            try {
	                minAmount = new BigDecimal(filter.getMinAmount());
	            } catch (NumberFormatException e) {
	                throw new IllegalArgumentException("Invalid minAmount value");
	            }
	        }
	        if (filter.getMaxAmount() != null && !filter.getMaxAmount().isBlank()) {
	            try {
	                maxAmount = new BigDecimal(filter.getMaxAmount());
	            } catch (NumberFormatException e) {
	                throw new IllegalArgumentException("Invalid maxAmount value");
	            }
	        }


	        final LocalDateTime finalFrom = fromDate;
	        final LocalDateTime finalTo = toDate;
	        final TransactionType finalType = transactionType;
	        final Category finalCat = category;
	        final BigDecimal finalMin = minAmount;
	        final BigDecimal finalMax = maxAmount;

	        List<FinancialRecord> filtered = records.stream()
	                .filter(r -> finalType == null || r.getType() == finalType)
	                .filter(r -> finalCat == null || r.getCategory() == finalCat)
	                .filter(r -> finalFrom == null || !r.getDate().isBefore(finalFrom))
	                .filter(r -> finalTo == null || !r.getDate().isAfter(finalTo))
	                .filter(r -> finalMin == null || r.getAmount().compareTo(finalMin) >= 0)
	                .filter(r -> finalMax == null || r.getAmount().compareTo(finalMax) <= 0)
	                .toList();

	    
	        BigDecimal totalIncome = filtered.stream()
	                .filter(r -> r.getType() == TransactionType.INCOME)
	                .map(FinancialRecord::getAmount)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	     
	        BigDecimal totalExpense = filtered.stream()
	                .filter(r -> r.getType() == TransactionType.EXPENSE)
	                .map(FinancialRecord::getAmount)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	       
	        BigDecimal netBalance = totalIncome.subtract(totalExpense);


	        Map<String, BigDecimal> categoryWiseTotals = new LinkedHashMap<>();
	        filtered.forEach(r -> {
	            String catKey = r.getCategory().name();
	            BigDecimal existing = categoryWiseTotals.getOrDefault(catKey, BigDecimal.ZERO);

	            BigDecimal contribution = (r.getType() == TransactionType.INCOME)
	                    ? r.getAmount()           
	                    : r.getAmount().negate(); 

	            categoryWiseTotals.put(catKey, existing.add(contribution));
	        });


	        Map<String, Map<String, BigDecimal>> typeAndCategoryWiseTotals = filtered.stream()
	                .collect(Collectors.groupingBy(
	                        r -> r.getType().name(),
	                        Collectors.groupingBy(
	                                r -> r.getCategory().name(),
	                                Collectors.reducing(
	                                        BigDecimal.ZERO,
	                                        FinancialRecord::getAmount,
	                                        BigDecimal::add)
	                        )
	                ));


	        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

	        Map<String, BigDecimal> monthlyTrends = new TreeMap<>(); 
	        filtered.forEach(r -> {
	            String monthKey = r.getDate().format(monthFormatter);
	            BigDecimal existing = monthlyTrends.getOrDefault(monthKey, BigDecimal.ZERO);

	            BigDecimal contribution = (r.getType() == TransactionType.INCOME)
	                    ? r.getAmount()
	                    : r.getAmount().negate();

	            monthlyTrends.put(monthKey, existing.add(contribution));
	        });

	       
	        DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");

	        Map<String, BigDecimal> weeklyTrends = new TreeMap<>();
	        filtered.forEach(r -> {
	            String weekKey = r.getDate().format(weekFormatter);
	            BigDecimal existing = weeklyTrends.getOrDefault(weekKey, BigDecimal.ZERO);

	            BigDecimal contribution = (r.getType() == TransactionType.INCOME)
	                    ? r.getAmount()
	                    : r.getAmount().negate();

	            weeklyTrends.put(weekKey, existing.add(contribution));
	        });


	        List<FinancialRecordResponse> recentActivities = filtered.stream()
	                .sorted(Comparator.comparing(FinancialRecord::getDate).reversed())
	                .limit(10)
	                .map(FinancialRecordResponse::FinancialRecord)
	                .toList();


	        return DashboardResponse.builder()
	                .totalIncome(totalIncome)
	                .totalExpense(totalExpense)
	                .netBalance(netBalance)
	                .categoryWiseTotals(categoryWiseTotals)
	                .typeAndCategoryWiseTotals(typeAndCategoryWiseTotals)
	                .monthlyTrends(monthlyTrends)
	                .weeklyTrends(weeklyTrends)
	                .recentActivities(recentActivities)
	                .totalRecords(filtered.size())
	                .appliedDateRange(appliedDateRange)
	                .build();
	    }

	 @Override
	    public ReportResponse generateReport(FinancialFilterRequest filter) {

	       
	        List<FinancialRecord> records;

	        if (filter.getUserId() == null || filter.getUserId().isBlank()) {
	            records = financialRecordRepository.findByActiveTrue();  // ALL users
	        } else {
	            userRepository.findById(filter.getUserId())
	                    .orElseThrow(() -> new UserNotFoundException(
	                            filter.getUserId(), "User not found"));
	            records = financialRecordRepository.findByUserIdAndActiveTrue(filter.getUserId());
	        }

	       
	        LocalDateTime fromDate = null;
	        LocalDateTime toDate = null;
	        String appliedDateRange = "All Time";

	        if (filter.getFrom() != null && !filter.getFrom().isBlank()) {
	            fromDate = LocalDateTime.parse(filter.getFrom());
	            toDate = (filter.getTo() != null && !filter.getTo().isBlank())
	                    ? LocalDateTime.parse(filter.getTo())
	                    : LocalDateTime.now();
	            appliedDateRange = filter.getFrom() + " to " + filter.getTo();

	        } else if (filter.getDays() != null && filter.getDays() > 0) {
	            fromDate = LocalDateTime.now().minusDays(filter.getDays());
	            toDate = LocalDateTime.now();
	            appliedDateRange = "Last " + filter.getDays() + " days";
	        }

	       
	        TransactionType transactionType = null;
	        if (filter.getType() != null && !filter.getType().isBlank()) {
	            try {
	                transactionType = TransactionType.valueOf(filter.getType().toUpperCase());
	            } catch (IllegalArgumentException e) {
	                throw new IllegalArgumentException(
	                        "Invalid type: '" + filter.getType() + "'. Use INCOME or EXPENSE");
	            }
	        }

	        Category category = null;
	        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
	            try {
	                category = Category.valueOf(filter.getCategory().toUpperCase());
	            } catch (IllegalArgumentException e) {
	                throw new IllegalArgumentException(
	                        "Invalid category: '" + filter.getCategory() + "'");
	            }
	        }

	        
	        BigDecimal minAmount = (filter.getMinAmount() != null && !filter.getMinAmount().isBlank())
	                ? new BigDecimal(filter.getMinAmount()) : null;
	        BigDecimal maxAmount = (filter.getMaxAmount() != null && !filter.getMaxAmount().isBlank())
	                ? new BigDecimal(filter.getMaxAmount()) : null;

	        
	        final LocalDateTime finalFrom = fromDate;
	        final LocalDateTime finalTo = toDate;
	        final TransactionType finalType = transactionType;
	        final Category finalCat = category;
	        final BigDecimal finalMin = minAmount;
	        final BigDecimal finalMax = maxAmount;

	        List<FinancialRecord> filtered = records.stream()
	                .filter(r -> finalType == null || r.getType() == finalType)
	                .filter(r -> finalCat == null || r.getCategory() == finalCat)
	                .filter(r -> finalFrom == null || !r.getDate().isBefore(finalFrom))
	                .filter(r -> finalTo == null || !r.getDate().isAfter(finalTo))
	                .filter(r -> finalMin == null || r.getAmount().compareTo(finalMin) >= 0)
	                .filter(r -> finalMax == null || r.getAmount().compareTo(finalMax) <= 0)
	                .toList();

	       
	        BigDecimal totalIncome = filtered.stream()
	                .filter(r -> r.getType() == TransactionType.INCOME)
	                .map(FinancialRecord::getAmount)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	        BigDecimal totalExpense = filtered.stream()
	                .filter(r -> r.getType() == TransactionType.EXPENSE)
	                .map(FinancialRecord::getAmount)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	        BigDecimal netBalance = totalIncome.subtract(totalExpense);

	      
	        Map<String, BigDecimal> categoryWiseTotals = new LinkedHashMap<>();
	        filtered.forEach(r -> {
	            String key = r.getCategory().name();
	            BigDecimal contribution = r.getType() == TransactionType.INCOME
	                    ? r.getAmount() : r.getAmount().negate();
	            categoryWiseTotals.merge(key, contribution, BigDecimal::add);
	        });

	       
	        Map<String, BigDecimal> typeWiseTotals = new LinkedHashMap<>();
	        typeWiseTotals.put("INCOME", totalIncome);
	        typeWiseTotals.put("EXPENSE", totalExpense);

	       
	        Map<String, Map<String, BigDecimal>> typeAndCategoryBreakdown = filtered.stream()
	                .collect(Collectors.groupingBy(
	                        r -> r.getType().name(),
	                        Collectors.groupingBy(
	                                r -> r.getCategory().name(),
	                                Collectors.reducing(BigDecimal.ZERO,
	                                        FinancialRecord::getAmount, BigDecimal::add)
	                        )
	                ));

	       
	        Map<String, ReportResponse.UserReportSummary> perUserSummary = new LinkedHashMap<>();

	        
	        Map<String, List<FinancialRecord>> groupedByUser = filtered.stream()
	                .collect(Collectors.groupingBy(r -> r.getUser().getId()));

	        groupedByUser.forEach((uid, userRecords) -> {

	            BigDecimal userIncome = userRecords.stream()
	                    .filter(r -> r.getType() == TransactionType.INCOME)
	                    .map(FinancialRecord::getAmount)
	                    .reduce(BigDecimal.ZERO, BigDecimal::add);

	            BigDecimal userExpense = userRecords.stream()
	                    .filter(r -> r.getType() == TransactionType.EXPENSE)
	                    .map(FinancialRecord::getAmount)
	                    .reduce(BigDecimal.ZERO, BigDecimal::add);

	          
	            Map<String, BigDecimal> userCategoryTotals = new LinkedHashMap<>();
	            userRecords.forEach(r -> {
	                String key = r.getCategory().name();
	                BigDecimal contribution = r.getType() == TransactionType.INCOME
	                        ? r.getAmount() : r.getAmount().negate();
	                userCategoryTotals.merge(key, contribution, BigDecimal::add);
	            });

	            perUserSummary.put(uid, ReportResponse.UserReportSummary.builder()
	                    .userId(uid)
	                    .userName(userRecords.get(0).getUser().getName())
	                    .totalRecords(userRecords.size())
	                    .totalIncome(userIncome)
	                    .totalExpense(userExpense)
	                    .netBalance(userIncome.subtract(userExpense))
	                    .categoryWiseTotals(userCategoryTotals)
	                    .build());
	        });

	       
	        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
	        Map<String, BigDecimal> monthlyTrends = new TreeMap<>();
	        filtered.forEach(r -> {
	            String key = r.getDate().format(monthFmt);
	            BigDecimal contribution = r.getType() == TransactionType.INCOME
	                    ? r.getAmount() : r.getAmount().negate();
	            monthlyTrends.merge(key, contribution, BigDecimal::add);
	        });

	       
	        DateTimeFormatter weekFmt = DateTimeFormatter.ofPattern("yyyy-'W'ww");
	        Map<String, BigDecimal> weeklyTrends = new TreeMap<>();
	        filtered.forEach(r -> {
	            String key = r.getDate().format(weekFmt);
	            BigDecimal contribution = r.getType() == TransactionType.INCOME
	                    ? r.getAmount() : r.getAmount().negate();
	            weeklyTrends.merge(key, contribution, BigDecimal::add);
	        });

	        
	        List<FinancialRecordResponse> allRecords = filtered.stream()
	                .sorted(Comparator.comparing(FinancialRecord::getDate).reversed())
	                .map(FinancialRecordResponse::FinancialRecord)
	                .toList();

	        ReportResponse response  =  ReportResponse.builder()
	                .reportGeneratedAt(LocalDateTime.now().toString())
	                .appliedDateRange(appliedDateRange)
	                .appliedUserId(filter.getUserId() == null || filter.getUserId().isBlank()
	                        ? "ALL" : filter.getUserId())
	                .appliedType(filter.getType() == null || filter.getType().isBlank()
	                        ? "ALL" : filter.getType().toUpperCase())
	                .appliedCategory(filter.getCategory() == null || filter.getCategory().isBlank()
	                        ? "ALL" : filter.getCategory().toUpperCase())
	                .totalRecords(filtered.size())
	                .totalIncome(totalIncome)
	                .totalExpense(totalExpense)
	                .netBalance(netBalance)
	                .categoryWiseTotals(categoryWiseTotals)
	                .typeWiseTotals(typeWiseTotals)
	                .typeAndCategoryBreakdown(typeAndCategoryBreakdown)
	                .perUserSummary(perUserSummary)
	                .monthlyTrends(monthlyTrends)
	                .weeklyTrends(weeklyTrends)
	                .records(allRecords)
	                .build();
	        
	        CompletableFuture<byte[]> pdfFuture = pdfReportUtil.generatePdfAsync(response);
	        response.setPdfFuture(pdfFuture);
	        
	        return response;
	    }
	

}
