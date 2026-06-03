package com.taskflow.approvalsystem.controller;

import com.taskflow.approvalsystem.entity.User;
import com.taskflow.approvalsystem.enums.Role;
import com.taskflow.approvalsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the specified details")
    public ResponseEntity<User> createUser(@RequestParam String name, @RequestParam String email, @RequestParam Role role, @RequestParam String department, @RequestParam(required = false) Long managerId) {
        try {
            User user = userService.createUser(name, email, role, department, managerId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
