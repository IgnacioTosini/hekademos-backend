package com.hekademos.hekademos_backend.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.hekademos.hekademos_backend.entities.Role;

public interface IRoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(String name);

}
