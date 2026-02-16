package com.hekademos.hekademos_backend.services;

import com.hekademos.hekademos_backend.entities.User;

public interface IUserService {
    User saveIfNotExists(String email, String name);

    User updateRoutineLink(String email, String routineLink);

    User getUserByEmail(String email);
}
