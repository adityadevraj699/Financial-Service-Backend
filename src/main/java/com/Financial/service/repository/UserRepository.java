package com.Financial.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Financial.service.entity.Users;
import com.Financial.service.entity.UsersRole;

public interface UserRepository extends JpaRepository<Users, String> {

	Optional<Users> findByEmail(String email);

	List<Users> findByRole(UsersRole analyst);

	Optional<Users> findByIdAndActiveTrue(String userId);

	List<Users> findByRoleAndActiveTrue(UsersRole analyst);

	List<Users> findByActiveTrue();
	

}
