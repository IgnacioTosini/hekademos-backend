package com.hekademos.hekademos_backend.services;

import java.util.List;

import com.hekademos.hekademos_backend.entities.User;

public interface IUserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    User getUserByEmail(String email);

    User getUserByRoutineLink(String routineLink);

    User saveIfNotExists(String email, String name, String picture);

    User updateRoutineLink(String email, String routineLink);

}
