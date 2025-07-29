package com.example.ddmdemo.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.ddmdemo.model.User;
import com.example.ddmdemo.respository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {this.userRepository = userRepository;}
	// Funkcija koja na osnovu username-a iz baze vraca objekat User-a
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = userRepository.findUserByUsername(username);
		
		if (user.isEmpty()) {
			throw new UsernameNotFoundException(String.format("No user found with email '%s'.", username));
		} 
		else return user.get();
	}
}