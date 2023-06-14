package com.amine.trix.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.amine.trix.model.Account;

public interface AccountRepository extends MongoRepository<Account, String> {
	Optional<Account> findByAppId(String appId);
	boolean existsByAppId(String appId);
}
