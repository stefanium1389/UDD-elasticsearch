package com.example.ddmdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ddmdemo.dto.ErrorDTO;
import com.example.ddmdemo.dto.LoginRequestDTO;
import com.example.ddmdemo.dto.LoginResponseDTO;
import com.example.ddmdemo.dto.UserDTO;
import com.example.ddmdemo.security.TokenUtils;
import com.example.ddmdemo.service.interfaces.FileService;
import com.example.ddmdemo.service.interfaces.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth/")
public class AuthController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private TokenUtils jwtTokenUtil;
	
	private final UserService userService;
	
	@PostMapping("login")
	public ResponseEntity<?> postLogin (@RequestBody LoginRequestDTO loginRequestDTO)
	{
		try
		{
		UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(),
				loginRequestDTO.getPassword());
		Authentication auth = authenticationManager.authenticate(authReq);

		SecurityContext sc = SecurityContextHolder.getContext();
		sc.setAuthentication(auth);

		String token = jwtTokenUtil.generateToken(loginRequestDTO.getUsername());
		String refreshToken = token;
		LoginResponseDTO response = new LoginResponseDTO(token, refreshToken);
				
		return new ResponseEntity<LoginResponseDTO>(response,HttpStatus.OK);
		}
		catch(AuthenticationException e)
		{
			ErrorDTO error = new ErrorDTO(e.getMessage());
			return new ResponseEntity<ErrorDTO>(error,HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping("register")
	public ResponseEntity<?> postRegister(@RequestBody LoginRequestDTO loginRequestDTO){
		try {
			UserDTO dto = userService.register(loginRequestDTO);
			return new ResponseEntity<UserDTO>(dto, HttpStatus.OK);
		}
		catch(Exception e) {
			ErrorDTO error = new ErrorDTO(e.getMessage());
			return new ResponseEntity<ErrorDTO>(error,HttpStatus.BAD_REQUEST);
		}
	}
	
}
