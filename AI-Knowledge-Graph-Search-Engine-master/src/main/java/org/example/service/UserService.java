package org.example.service;

import org.example.model.User;
import org.example.model.enums.UserRole;
import org.example.repository.UserRepository;
import org.example.util.SecurityUtils;
import org.example.util.ValidationUtils;
import org.example.exception.ValidationException;
import org.example.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public User createUser(String username, String email, String password, String fullName, UserRole role) {
        // Validate inputs
        ValidationUtils.validateUsername(username);
        ValidationUtils.validateEmail(email);
        ValidationUtils.validateNotEmpty(password, "Password");
        ValidationUtils.validateNotEmpty(fullName, "Full name");

        // Check if username or email already exists
        if (userRepository.findByUsername(username) != null) {
            throw new ValidationException("Username already exists");
        }

        // Create user
        User user = new User();
        user.setId(SecurityUtils.generateId());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(SecurityUtils.hashPassword(password));
        user.setFullName(fullName);
        user.setRole(role != null ? role : UserRole.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getUserById(String id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return user;
    }

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User updateUser(User user) {
        ValidationUtils.validateNotNull(user, "User");
        ValidationUtils.validateNotNull(user.getId(), "User ID");

        // Verify user exists
        getUserById(user.getId());

        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        getUserById(id); // Verify exists
        userRepository.delete(id);
    }

    public boolean authenticate(String username, String password) {
        try {
            User user = getUserByUsername(username);
            if (!user.isActive()) {
                return false;
            }

            boolean authenticated = SecurityUtils.verifyPassword(password, user.getPassword());
            if (authenticated) {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
            }
            return authenticated;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!SecurityUtils.verifyPassword(oldPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        if (!SecurityUtils.isStrongPassword(newPassword)) {
            throw new ValidationException(
                    "New password must be at least 8 characters and contain uppercase, lowercase, digit, and special character");
        }

        user.setPassword(SecurityUtils.hashPassword(newPassword));
        userRepository.save(user);
    }

    public void deactivateUser(String userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(String userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
    }
}
