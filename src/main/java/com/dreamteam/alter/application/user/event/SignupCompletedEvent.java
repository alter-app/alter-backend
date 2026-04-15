package com.dreamteam.alter.application.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupCompletedEvent {
    private String signupSessionId;
    private String contact;
}
