package com.amine.trix.model;

import java.util.HashSet;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document("users")
public class User {
	@Id
	private String id;
	@Indexed(unique = true)
	private String facebookId;
	@Indexed(unique = true)
	private String email;
	private String name;
	private HashSet<Role> roles;
}
