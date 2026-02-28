package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.port.outbound.FirebaseTokenVerifier;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component("firebaseTokenVerifier")
@RequiredArgsConstructor
public class FirebaseTokenVerifierImpl implements FirebaseTokenVerifier {

    private static final String PHONE_NUMBER_CLAIM = "phone_number";
    private static final String USED_TOKEN_KEY_PREFIX = "FIREBASE:USED:";
    private static final long USED_TOKEN_EXPIRATION_HOURS = 1;

    private final FirebaseApp firebaseApp;
    private final StringRedisTemplate redisTemplate;

    @Override
    public String verifyAndGetPhoneNumber(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance(firebaseApp).verifyIdToken(idToken);

            // 토큰 재사용 방지
            String tokenKey = USED_TOKEN_KEY_PREFIX + decodedToken.getUid() + ":" + getAuthTime(decodedToken);
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(
                tokenKey, "1", USED_TOKEN_EXPIRATION_HOURS, TimeUnit.HOURS
            );

            if (Boolean.FALSE.equals(isNew)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED, "이미 사용된 Firebase 인증 토큰입니다.");
            }

            Object phoneNumber = decodedToken.getClaims().get(PHONE_NUMBER_CLAIM);

            if (phoneNumber == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED, "Firebase 토큰에 전화번호 정보가 없습니다.");
            }

            return (String) phoneNumber;
        } catch (FirebaseAuthException e) {
            log.warn("Firebase ID Token 검증 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.UNAUTHORIZED, "Firebase 인증 토큰이 유효하지 않습니다.");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Firebase 토큰 검증 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private long getAuthTime(FirebaseToken decodedToken) {
        Object authTime = decodedToken.getClaims().get("auth_time");
        if (authTime instanceof Number) {
            return ((Number) authTime).longValue();
        }
        return 0L;
    }
}
