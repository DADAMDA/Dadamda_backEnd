package com.example.Oauth.common.user;

import com.example.Oauth.common.exception.ResourceNotFoundException;
import com.example.Oauth.user.User;
import com.example.Oauth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FixedCurrentUserProvider {

    private static final Long FIXED_USER_ID = 1L;

    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        return FIXED_USER_ID;
    }

    public User getCurrentUser() {
        return userRepository.findById(FIXED_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + FIXED_USER_ID));
    }
}
