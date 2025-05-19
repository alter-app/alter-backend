package com.dreamteam.alter.common.util;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CursorUtil {

    public static String encodeCursor(Object cursor, ObjectMapper objectMapper) {
        try {
            String encoded = objectMapper.writeValueAsString(cursor);
            return Base64.getEncoder().encodeToString(encoded.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public static <T> T decodeCursor(String cursor, Class<T> clazz, ObjectMapper objectMapper) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cursor);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

}
