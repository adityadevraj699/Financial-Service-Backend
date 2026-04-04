package com.Financial.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Financial.service.entity.FinancialRecord;
import com.Financial.service.entity.TransactionType;
import com.Financial.service.entity.Users;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, String> {

	List<FinancialRecord> findByUserId(String userId);
	
	List<FinancialRecord> findByUserIdAndActiveTrue(String userId);
	List<FinancialRecord> findByActiveTrue();

}
