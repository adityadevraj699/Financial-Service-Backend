package com.Financial.service.service.Impl;

import org.springframework.stereotype.Service;

import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.UsersRole;
import com.Financial.service.exception.UserNotFoundException;
import com.Financial.service.repository.UserRepository;
import com.Financial.service.service.ViewersService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ViewersServiceImpl implements ViewersService {
	
	private final UserRepository userRepository;

	@Override
	public UserDto getViewerById(String userId) {
		return userRepository.findById(userId)
				.filter(user -> user.getRole() == UsersRole.VIEWER)
				.map(com.Financial.service.dto.UserDto::users)
				.orElseThrow(() -> new UserNotFoundException("Viewer not found with id: " + userId));
	}

}
