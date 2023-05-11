package com.amine.trix.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.amine.trix.model.Game;

public interface GameRepository extends MongoRepository<Game, String> {

}
