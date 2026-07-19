package com.quizsphere.controller;

import com.quizsphere.dto.ProfileResponse;
import com.quizsphere.dto.UpdateProfileRequest;
import com.quizsphere.entity.User;
import com.quizsphere.service.CurrentUserService;
import com.quizsphere.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final CurrentUserService currentUserService;
    private final ProfileService profileService;

    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {
        User user = currentUserService.require(authentication);
        return profileService.get(user);
    }

    @PutMapping
    public ProfileResponse updateProfile(Authentication authentication,
                                         @Valid @RequestBody UpdateProfileRequest request) {
        User user = currentUserService.require(authentication);
        return profileService.update(user, request);
    }
}
