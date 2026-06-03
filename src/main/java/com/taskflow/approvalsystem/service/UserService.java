package com.taskflow.approvalsystem.service;

import com.taskflow.approvalsystem.entity.User;
import com.taskflow.approvalsystem.enums.Role;
import com.taskflow.approvalsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(String name, String email, Role role, String department, Long managerId) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        User manager = null;
        if (managerId != null) {
            manager = userRepository.findById(managerId).orElseThrow(() -> new IllegalArgumentException("Manager not found"));
        }
        if (role == Role.EMPLOYEE && manager == null) {
            throw new IllegalArgumentException("Employee must have a manager");
        }
        User user = new User(name, email, role, department, manager);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
