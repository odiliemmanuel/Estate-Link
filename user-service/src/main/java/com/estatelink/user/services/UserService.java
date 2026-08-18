package com.estatelink.user.services;

import com.estatelink.user.data.model.Role;
import com.estatelink.user.data.model.User;
import com.estatelink.user.data.model.UserStatus;
import com.estatelink.user.data.repository.UserRepository;
import com.estatelink.user.dtos.requests.RegisterRequest;
import com.estatelink.user.dtos.responses.UserResponse;
import com.estatelink.user.event.UserRegisteredEvent;
import com.estatelink.user.eventProducer.EventProducer;
import com.estatelink.user.utils.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private  UserMapper userMapper;

    private final EventProducer eventProducer;

    // ── Auth ────────────────────────────────────────────────
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

//        if(!request.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
//            throw new IllegalArgumentException("Invalid email format");
//        }
//
//        if(!request.getName().matches("^[a-zA-Z\\s]+$")) {
//            throw new IllegalArgumentException("Name can only contain letters and spaces");
//        }
//
//        if(!request.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
//            throw new IllegalArgumentException("Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character");
//        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.UNVERIFIED)
                .verificationToken(verificationToken)
                .tokenExpiresAt(LocalDateTime.now().plusHours(24))
                .build();

        userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(user.getId(), user.getName(), user.getEmail(), verificationToken, LocalDateTime.now());
        eventProducer.send(event);

        return userMapper.toResponse(user);
    }

    public User findByEmailForAuth(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }


    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (user.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationToken(null);     // single-use — clear it
        user.setTokenExpiresAt(null);
        userRepository.save(user);
    }

    // ── Profile ─────────────────────────────────────────────

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(UUID id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    // ── Admin ────────────────────────────────────────────────

    public List<UserResponse> getAllAgents() {
        return userRepository.findByRole(Role.AGENT)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUserStatus(UUID id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setStatus(status);
        return userMapper.toResponse(userRepository.save(user));
    }

    public List<UserResponse> getPendingAgents() {
        return userRepository.findByRoleAndStatus(Role.AGENT, UserStatus.UNVERIFIED)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
}