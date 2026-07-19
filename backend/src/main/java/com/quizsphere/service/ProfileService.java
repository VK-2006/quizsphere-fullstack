package com.quizsphere.service;

import com.quizsphere.dto.ProfileResponse;
import com.quizsphere.dto.UpdateProfileRequest;
import com.quizsphere.entity.QuizAttempt;
import com.quizsphere.entity.User;
import com.quizsphere.exception.BadRequestException;
import com.quizsphere.repository.QuizAttemptRepository;
import com.quizsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final QuizAttemptRepository attemptRepository;

    @Transactional(readOnly = true)
    public ProfileResponse get(User user) {
        return response(user);
    }

    @Transactional
    public ProfileResponse update(User user, UpdateProfileRequest request) {
        if (request.dateOfBirth() != null && request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new BadRequestException("Date of birth cannot be in the future");
        }
        user.setFullName(request.fullName().trim());
        user.setBio(clean(request.bio()));
        user.setPhone(clean(request.phone()));
        user.setLocation(clean(request.location()));
        user.setDateOfBirth(request.dateOfBirth());
        user.setAvatarUrl(clean(request.avatarUrl()));
        return response(userRepository.save(user));
    }

    private ProfileResponse response(User user) {
        List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
                .filter(a -> a.getSubmittedAt() != null)
                .toList();
        long passed = attempts.stream().filter(QuizAttempt::isPassed).count();
        double average = attempts.stream().mapToDouble(QuizAttempt::getPercentage).average().orElse(0.0);
        average = Math.round(average * 10.0) / 10.0;
        return new ProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(),
                user.isEnabled(), user.getAuthProvider().name(), user.getAvatarUrl(), user.getBio(),
                user.getPhone(), user.getLocation(), user.getDateOfBirth(), user.getCreatedAt(),
                attempts.size(), passed, average);
    }

    private String clean(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
