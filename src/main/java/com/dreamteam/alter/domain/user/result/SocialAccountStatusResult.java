package com.dreamteam.alter.domain.user.result;

import com.dreamteam.alter.domain.user.type.SocialProvider;

import java.time.LocalDateTime;

public record SocialAccountStatusResult(
    SocialProvider provider,
    boolean linked,
    LocalDateTime linkedAt
) {
}
