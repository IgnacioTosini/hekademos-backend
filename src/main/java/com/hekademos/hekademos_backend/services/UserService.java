package com.hekademos.hekademos_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hekademos.hekademos_backend.entities.User;
import com.hekademos.hekademos_backend.repositories.IUserRepository;
import com.hekademos.hekademos_backend.repositories.IRoleRepository;
import com.hekademos.hekademos_backend.entities.Role;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByRoutineLink(String routineLink) {
        Optional<User> user = userRepository.findByRoutineLink(routineLink);
        return user.orElse(null);
    }

    @Override
    @Transactional
    public User saveIfNotExists(String email, String name, String picture) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("No existe el rol ROLE_USER en la base de datos"));
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                List<Role> roles = new ArrayList<>();
                roles.add(userRole);
                user.setRoles(roles);
                userRepository.save(user);
            }
            return user;
        }
    User newUser = new User();
    newUser.setEmail(email);
    newUser.setName(name);
    newUser.setPicture(picture);
    List<Role> roles = new ArrayList<>();
    roles.add(userRole);
    newUser.setRoles(roles);
    return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public User updateRoutineLink(String email, String routineLink) {
        Optional<User> user = userRepository.findByEmail(email);
        Optional<User> existingUser = userRepository.findByRoutineLink(routineLink);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("El enlace de rutina ya está en uso por otro usuario.");
        }
        if (user.isPresent()) {
            User updatedUser = user.get();
            updatedUser.setRoutineLink(routineLink);
            userRepository.save(updatedUser);
            return updatedUser;
        }
        return null;
    }

}
