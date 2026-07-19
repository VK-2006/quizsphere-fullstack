package com.quizsphere.dto;

public record UserResponse(Long id, String fullName, String email, String role, boolean enabled) {}
