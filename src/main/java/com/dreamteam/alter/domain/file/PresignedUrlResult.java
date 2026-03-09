package com.dreamteam.alter.domain.file;

import java.time.Instant;

public record PresignedUrlResult(String url, Instant expiresAt) {}
