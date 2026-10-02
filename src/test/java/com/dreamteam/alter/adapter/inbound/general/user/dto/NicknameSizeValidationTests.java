package com.dreamteam.alter.adapter.inbound.general.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("닉네임 길이 검증 테스트")
class NicknameSizeValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    static Stream<Class<?>> nicknameDtos() {
        return Stream.of(
            CreateUserRequestDto.class,
            CreateUserWithSocialRequestDto.class,
            CheckNicknameDuplicationRequestDto.class,
            UpdateNicknameRequestDto.class,
            com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdateNicknameRequestDto.class
        );
    }

    static Stream<Arguments> invalidNicknames() {
        return nicknameDtos().flatMap(dto -> Stream.of(
            Arguments.of(dto, "가"),
            Arguments.of(dto, "가나다라마바사아자차카")
        ));
    }

    static Stream<Arguments> validNicknames() {
        return nicknameDtos().flatMap(dto -> Stream.of(
            Arguments.of(dto, "가나"),
            Arguments.of(dto, "가나다라마바사아자차")
        ));
    }

    @ParameterizedTest(name = "{0} - \"{1}\"")
    @MethodSource("invalidNicknames")
    @DisplayName("닉네임이 2자 미만이거나 10자를 넘으면 검증에 실패한다")
    void rejectsNicknameOutOfRange(Class<?> dto, String nickname) {
        assertThat(validator.validateValue(dto, "nickname", nickname)).isNotEmpty();
    }

    @ParameterizedTest(name = "{0} - \"{1}\"")
    @MethodSource("validNicknames")
    @DisplayName("닉네임이 2~10자면 검증을 통과한다")
    void acceptsNicknameInRange(Class<?> dto, String nickname) {
        assertThat(validator.validateValue(dto, "nickname", nickname)).isEmpty();
    }
}
