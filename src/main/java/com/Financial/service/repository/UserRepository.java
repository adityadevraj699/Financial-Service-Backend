package com.Financial.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Financial.service.entity.Users;

public interface UserRepository extends JpaRepository<Users, String> {

	Optional<Users> findByEmail(String email);
	
	// create users
	//update users
	//delete users
	//get users by id
	//get all users
	//get all analysts users
	//get all viewer users

}
