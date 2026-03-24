package com.example.random_major.controller;

import com.example.random_major.entity.User;
import com.example.random_major.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            
            User user = userService.signup(name, email, password);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User registered successfully",
                "userId", user.getId(),
                "name", user.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            User user = userService.signin(email, password);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged in successfully",
                "userId", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
