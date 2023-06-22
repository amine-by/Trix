package com.amine.trix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.amine.trix.model.Game;

public interface GameRepository extends MongoRepository<Game, String> {
	@Query(value = "{ 'players.3': { $exists: false } }", fields = "{}" )
	Page<Game> findAvailableGames(Pageable pageable);
}
