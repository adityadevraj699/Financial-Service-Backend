package com.Financial.service.service.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Financial.service.config.JwtService;
import com.Financial.service.dto.AuthResponse;
import com.Financial.service.dto.UserDto;
import com.Financial.service.entity.Users;
import com.Financial.service.exception.InvalidCredentialsException;
import com.Financial.service.exception.UserNotFoundException;
import com.Financial.service.repository.UserRepository;
import com.Financial.service.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Override
	public Users createUser(UserDto userDto) {
		// TODO Auto-generated method stub
		return null;
	}

	public AuthResponse login(String email, String password) {
        
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));  

       
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Your account are not Active at Time, Please Contact Admin");                   
        }

        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();                  
        }

        
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(UserDto.users(user))
                .build();
    }

	@Override
	public Users updateUser(String id, UserDto userDto) {
		
		return null;
	}

	@Override
	public void deleteUser(String id) {
		
		
	}

	@Override
	public List<UserDto> getAllUsers() {
		
		return null;
	}

	@Override
	public UserDto getUserById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserDto> getUsersByRole(String role) {
		// TODO Auto-generated method stub
		return null;
	}

}
