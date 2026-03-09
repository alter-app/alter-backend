package com.dreamteam.alter.adapter.outbound.file.redis;

import com.dreamteam.alter.domain.file.PresignedUrlResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

record PresignedUrlCacheEntry(
    @JsonProperty("url") String url,
    @JsonProperty("expiresAt") long expiresAt
) {
    static PresignedUrlCacheEntry from(PresignedUrlResult result) {
        return new PresignedUrlCacheEntry(result.url(), result.expiresAt().toEpochMilli());
    }

    PresignedUrlResult toResult() {
        return new PresignedUrlResult(url, Instant.ofEpochMilli(expiresAt));
    }
}
