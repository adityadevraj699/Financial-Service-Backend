package com.Financial.service.service;

import java.util.List;

import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.UsersRole;

public interface AnalystService {
	
	// get all users viewers
	List<UserDto> getAllUsers();
	

}
