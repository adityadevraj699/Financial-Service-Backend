package com.Financial.service.service.Impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.UsersRole;
import com.Financial.service.repository.UserRepository;
import com.Financial.service.service.AnalystService;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class AnalystServiceImpl implements AnalystService {
	
	private final UserRepository userRepository;

	@Override
	public List<UserDto> getAllUsers() {
		return userRepository.findByRoleAndActiveTrue(UsersRole.VIEWER).stream()
				.filter(user -> user.getRole() != UsersRole.ADMIN)
				.map(UserDto::users)
				.toList();
	}


}
