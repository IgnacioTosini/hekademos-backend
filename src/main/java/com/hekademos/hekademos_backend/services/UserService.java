package com.hekademos.hekademos_backend.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hekademos.hekademos_backend.entities.User;
import com.hekademos.hekademos_backend.repositories.IUserRepository;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Override
    @Transactional
    public User saveIfNotExists(String email, String name) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public User updateRoutineLink(String email, String routineLink) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            User updatedUser = user.get();
            updatedUser.setRoutineLink(routineLink);
            userRepository.save(updatedUser);
            return updatedUser;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

}
