package com.legoproject.backend;

import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/register")
    public User register(@RequestBody User user) {

        return userService.register(
                user.getUsername(),
                user.getPassword()
        );
    }

    @PostMapping("/api/login")
    public User login(@RequestBody User user) {

        return userService.login(
                user.getUsername(),
                user.getPassword()
        );
    }
}