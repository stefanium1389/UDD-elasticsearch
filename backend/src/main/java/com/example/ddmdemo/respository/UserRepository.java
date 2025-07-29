package com.example.ddmdemo.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ddmdemo.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	Optional<User> findUserByUsername(String username);
}
