package com.estatelink.user.controllers;

import com.estatelink.user.data.model.User;
import com.estatelink.user.data.model.UserStatus;
import com.estatelink.user.dtos.requests.LoginRequest;
import com.estatelink.user.dtos.requests.RegisterRequest;
import com.estatelink.user.dtos.responses.AuthResponse;
import com.estatelink.user.dtos.responses.UserResponse;
import com.estatelink.user.services.JwtService;
import com.estatelink.user.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
        }
        catch (Exception exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByEmailForAuth(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }

        String token = jwtService.generateToken(user);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userService.getUserById(user.getId()))
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {

        userService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }
}