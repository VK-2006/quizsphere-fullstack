package com.quizsphere.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.quizsphere.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenService {
    private final String clientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenService(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.verifier = this.clientId.isBlank() ? null : new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(this.clientId))
                .build();
    }

    public GoogleProfile verify(String credential) {
        if (verifier == null) {
            throw new BadRequestException("Google Sign-In is not configured on the server");
        }
        try {
            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                throw new BadRequestException("Invalid Google credential");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new BadRequestException("Google email is not verified");
            }
            String email = payload.getEmail();
            String subject = payload.getSubject();
            if (email == null || subject == null) {
                throw new BadRequestException("Google account information is incomplete");
            }
            String name = value(payload.get("name"));
            String picture = value(payload.get("picture"));
            return new GoogleProfile(subject, email.trim().toLowerCase(), name, picture);
        } catch (GeneralSecurityException | IOException ex) {
            throw new BadRequestException("Could not verify Google credential");
        }
    }

    private String value(Object value) {
        return value == null ? null : value.toString();
    }

    public record GoogleProfile(String subject, String email, String fullName, String avatarUrl) {}
}
