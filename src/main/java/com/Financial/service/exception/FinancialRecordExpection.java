package com.Financial.service.exception;

public class FinancialRecordExpection extends RuntimeException {
	
	 public FinancialRecordExpection(String id) {
	        super("Financial Record not Found on this Id : " + id);
	    }
	    
	     public FinancialRecordExpection(String Id, String message) {
			super("Financial Record with ID " + Id + " not found. " + message);
		}

}
