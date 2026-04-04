package com.Financial.service.service;

import java.util.List;

import com.Financial.service.dto.CreateUserRequest;
import com.Financial.service.dto.FinancialRecordRequest;
import com.Financial.service.dto.FinancialRecordResponse;
import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.UsersRole;

import jakarta.validation.Valid;

public interface AdminService {
	
	//create users
	UserDto createUser(CreateUserRequest request);
	
	//active & deactive users
	String setActiveStatus(String userId);
	
	//get all users
	List<UserDto> getAllUsers(UsersRole role);
	
	// update role
	UserDto updateUserRole(String userId, String role);


	
	//

}
