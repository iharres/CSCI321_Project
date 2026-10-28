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
    public UserResponse register(@RequestBody User user) {

        User registeredUser = userService.register(user.getUsername(), user.getPassword());
        return new UserResponse(registeredUser.getId(), registeredUser.getUsername());
    }

    @PostMapping("/api/login")
    public UserResponse login(@RequestBody User user) {
        User loggedInUser = userService.login(user.getUsername(), user.getPassword());
        return new UserResponse(loggedInUser.getId(), loggedInUser.getUsername());
    }
}