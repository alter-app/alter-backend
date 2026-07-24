package com.dreamteam.alter.domain.workspace.entity;

import org.springframework.test.util.ReflectionTestUtils;

/**
 * BusinessType 테스트 픽스처.
 * requiresDetail = true 인 '기타' 는 시드로만 생성되어 create() 로는 만들 수 없으므로 리플렉션으로 세팅한다.
 */
public final class BusinessTypeFixture {

    private BusinessTypeFixture() {
    }

    public static BusinessType of(boolean requiresDetail) {
        BusinessType businessType = BusinessType.create(requiresDetail ? "기타" : "카페", null);
        ReflectionTestUtils.setField(businessType, "requiresDetail", requiresDetail);
        return businessType;
    }
}
