package com.Financial.service.service;

import com.Financial.service.dto.UserDto;

public interface ViewersService {
	
	//get viewers by id
	UserDto getViewerById(String userId);

}
