package com.example.ddmdemo.service.interfaces;

import com.example.ddmdemo.dto.LoginRequestDTO;
import com.example.ddmdemo.dto.UserDTO;

public interface UserService {
	UserDTO register(LoginRequestDTO loginRequestDTO);
}
