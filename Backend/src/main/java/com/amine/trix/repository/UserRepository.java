package com.amine.trix.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.amine.trix.model.User;

public interface UserRepository extends MongoRepository<User, String> {
	Optional<User> findByAppId(String appId);
	boolean existsByAppId(String appId);
}
