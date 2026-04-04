// UserNotFoundException.java
package com.Financial.service.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }
    
     public UserNotFoundException(String userId, String message) {
		super("User with ID " + userId + " not found. " + message);
	}
}