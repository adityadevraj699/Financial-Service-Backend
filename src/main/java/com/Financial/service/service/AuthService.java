package com.Financial.service.service;

import java.util.List;

import com.Financial.service.dto.AuthResponse;
import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.Users;

public interface AuthService {
	
	
	//login user
	AuthResponse login(String email, String password);
	


}
