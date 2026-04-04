package com.Financial.service.service.Impl;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Financial.service.config.JwtService;
import com.Financial.service.dto.CreateUserRequest;
import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.Users;
import com.Financial.service.entity.UsersRole;
import com.Financial.service.exception.UserAlreadyExistsException;
import com.Financial.service.exception.UserNotFoundException;
import com.Financial.service.repository.UserRepository;
import com.Financial.service.service.AdminService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	

	@Override
	public UserDto createUser(CreateUserRequest request) {

	    var defaultPassword = request.getRole();

	    Users user = Users.builder()
	            .name(request.getName())
	            .email(request.getEmail())
	            .password(passwordEncoder.encode(String.valueOf(defaultPassword).toUpperCase()))
	            .role(request.getRole())
	            .active(true)
	            .build();

	    try {
	        Users savedUser = userRepository.save(user);
	        return UserDto.users(savedUser);

	    } catch (DataIntegrityViolationException ex) {

	        
	        Throwable rootCause = ex.getRootCause();

	        if (rootCause != null && rootCause.getMessage().toLowerCase().contains("email")) {
	            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
	        }

	       
	        throw new IllegalStateException("Something went wrong while creating user", ex);
	    }
	}


	@Transactional
	@Override
	public String setActiveStatus(String userId) {

		Users user = userRepository.findById(userId)
	            .orElseThrow(() -> new UserNotFoundException(userId));

	    user.setActive(!user.isActive());

	    return "User is now " + (user.isActive() ? "active" : "inactive");
	}



	@Transactional
	@Override
	public UserDto updateUserRole(String userId, String role) {
		Users user = userRepository.findByIdAndActiveTrue(userId)
				.orElseThrow(() -> new UserNotFoundException(userId, "User not found !!"));
		

		UsersRole newRole;
		try {
			
			newRole = UsersRole.valueOf(role.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid role: " + role);
		}
		
		if (user.getRole() == newRole) {
		    throw new IllegalStateException("User already has role: " + newRole);
		}

		user.setRole(newRole);
		return UserDto.users(user);
	}



	@Override
	public List<UserDto> getAllUsers(UsersRole role) {

	    if (role == null) {
	        return userRepository.findByActiveTrue().stream()
	                .filter(user -> user.getRole() != UsersRole.ADMIN)
	                .map(UserDto::users)
	                .toList();
	    }

	    return userRepository.findByRoleAndActiveTrue(role).stream()
	            .filter(user -> user.getRole() != UsersRole.ADMIN)
	            .map(UserDto::users)
	            .toList();
	}

}
