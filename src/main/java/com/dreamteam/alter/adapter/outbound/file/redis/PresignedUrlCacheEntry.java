package com.dreamteam.alter.adapter.outbound.file.redis;

import com.dreamteam.alter.domain.file.PresignedUrlResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

record PresignedUrlCacheEntry(
    @JsonProperty("url") String url,
    @JsonProperty("expiresAtEpochSecond") long expiresAtEpochSecond
) {
    static PresignedUrlCacheEntry from(PresignedUrlResult result) {
        return new PresignedUrlCacheEntry(result.url(), result.expiresAt().getEpochSecond());
    }

    PresignedUrlResult toResult() {
        return new PresignedUrlResult(url, Instant.ofEpochSecond(expiresAtEpochSecond));
    }
}
