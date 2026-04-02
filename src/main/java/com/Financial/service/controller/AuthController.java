package com.Financial.service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Financial.service.dto.ApiResponse;
import com.Financial.service.dto.AuthResponse;
import com.Financial.service.dto.LoginRequest;
import com.Financial.service.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private AuthService authService;
	
	public AuthController(AuthService authService) {
		this.authService = authService;
	}
	
	@PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity
                .status(HttpStatus.OK)               
                .body(ApiResponse.success("Login successful", authResponse));
    }

}
