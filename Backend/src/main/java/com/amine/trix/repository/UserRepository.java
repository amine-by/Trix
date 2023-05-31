package com.amine.trix.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.amine.trix.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	Optional<User> findByFacebookId(String facebookId);
	boolean existsByFacebookId(String facebookId);
}
