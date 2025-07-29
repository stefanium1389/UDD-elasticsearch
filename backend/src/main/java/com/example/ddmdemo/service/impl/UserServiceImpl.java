package com.example.ddmdemo.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ddmdemo.dto.LoginRequestDTO;
import com.example.ddmdemo.dto.UserDTO;
import com.example.ddmdemo.model.User;
import com.example.ddmdemo.respository.UserRepository;
import com.example.ddmdemo.service.interfaces.UserService;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public UserDTO register(LoginRequestDTO dto) {
		Optional<User> optuser = userRepo.findUserByUsername(dto.getUsername());
		if (optuser.isPresent()) {
			throw new RuntimeException("Username already exists");
		}
		User user = new User();
		user.setUsername(dto.getUsername());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		
		userRepo.save(user);
		userRepo.flush();
		
		return new UserDTO(user.getUsername());
	}

}
