package com.dreamteam.alter.domain.auth.port.outbound;

public interface FirebaseTokenVerifier {
    String verifyAndGetPhoneNumber(String idToken);
}
