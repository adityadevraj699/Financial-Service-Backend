package com.Financial.service.service;

import java.util.List;

import com.Financial.service.dto.AuthResponse;
import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.Users;

public interface AuthService {
	
	//create user
	Users createUser(UserDto userDto);
	
	//login user
	AuthResponse login(String email, String password);
	
	//update user
	Users updateUser(String id, UserDto userDto);
	
	//delete user
	void deleteUser(String id);
	
	// get all users
	List<UserDto> getAllUsers();
	
	// get user by id
	UserDto getUserById(String id);
	
	// get all users by role
	List<UserDto> getUsersByRole(String role);


}
