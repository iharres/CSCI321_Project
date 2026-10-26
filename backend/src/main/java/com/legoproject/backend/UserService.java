package com.legoproject.backend;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        User user = new User(username, password);

        return userRepository.save(user);
    }

    public User login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username or password"
                        )
                );

        if (!user.getPassword().equals(password)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }

        return user;
    }
}