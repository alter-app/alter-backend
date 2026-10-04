package com.dreamteam.alter.adapter.inbound.general.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("닉네임 검증 테스트")
class NicknameValidationTests {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    static Stream<Class<?>> createUpdateDtos() {
        return Stream.of(
            CreateUserRequestDto.class,
            CreateUserWithSocialRequestDto.class,
            UpdateNicknameRequestDto.class,
            com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdateNicknameRequestDto.class
        );
    }

    static Stream<Class<?>> duplicationCheckDtos() {
        return Stream.of(CheckNicknameDuplicationRequestDto.class);
    }

    @Nested
    @DisplayName("생성 및 변경 DTO")
    class CreateUpdateDtoTests {

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @DisplayName("유효하지 않은 닉네임")
        class InvalidNicknameTests {

            Stream<Arguments> invalidNicknames() {
                return createUpdateDtos().flatMap(dto -> Stream.of(
                    Arguments.of(dto, null),
                    Arguments.of(dto, ""),
                    Arguments.of(dto, "  "),
                    Arguments.of(dto, "가"),
                    Arguments.of(dto, "가나다라마바사아자차카"),
                    Arguments.of(dto, " 가나"),
                    Arguments.of(dto, "가나 "),
                    Arguments.of(dto, "가 나"),
                    Arguments.of(dto, "가나😀"),
                    Arguments.of(dto, "가나!")
                ));
            }

            @ParameterizedTest(name = "{0} - \"{1}\"")
            @MethodSource("invalidNicknames")
            @DisplayName("필수 입력, 길이 또는 허용 문자 규칙을 위반하면 검증에 실패한다")
            void validate_nicknameInvalid_hasViolations(Class<?> dto, String nickname) {
                // given
                String propertyName = "nickname";

                // when
                var violations = validator.validateValue(dto, propertyName, nickname);

                // then
                assertThat(violations).isNotEmpty();
            }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @DisplayName("유효한 닉네임")
        class ValidNicknameTests {

            Stream<Arguments> validNicknames() {
                return createUpdateDtos().flatMap(dto -> Stream.of(
                    Arguments.of(dto, "가나"),
                    Arguments.of(dto, "가나다라마바사아자차"),
                    Arguments.of(dto, "abc12"),
                    Arguments.of(dto, "가a1")
                ));
            }

            @ParameterizedTest(name = "{0} - \"{1}\"")
            @MethodSource("validNicknames")
            @DisplayName("2~10자의 한글, 영문, 숫자 닉네임은 검증을 통과한다")
            void validate_nicknameValid_hasNoViolations(Class<?> dto, String nickname) {
                // given
                String propertyName = "nickname";

                // when
                var violations = validator.validateValue(dto, propertyName, nickname);

                // then
                assertThat(violations).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("중복 확인 DTO")
    class DuplicationCheckDtoTests {

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @DisplayName("유효하지 않은 닉네임")
        class InvalidNicknameTests {

            Stream<Arguments> invalidNicknames() {
                return duplicationCheckDtos().flatMap(dto -> Stream.of(
                    Arguments.of(dto, null),
                    Arguments.of(dto, ""),
                    Arguments.of(dto, "  "),
                    Arguments.of(dto, "가".repeat(65))
                ));
            }

            @ParameterizedTest(name = "{0} - \"{1}\"")
            @MethodSource("invalidNicknames")
            @DisplayName("닉네임이 비어 있거나 64자를 넘으면 검증에 실패한다")
            void validate_nicknameBlankOrTooLong_hasViolations(Class<?> dto, String nickname) {
                // given
                String propertyName = "nickname";

                // when
                var violations = validator.validateValue(dto, propertyName, nickname);

                // then
                assertThat(violations).isNotEmpty();
            }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @DisplayName("유효한 닉네임")
        class ValidNicknameTests {

            Stream<Arguments> validNicknames() {
                return duplicationCheckDtos().flatMap(dto -> Stream.of(
                    Arguments.of(dto, "가"),
                    Arguments.of(dto, "가나다라마바사아자차카타"),
                    Arguments.of(dto, "가".repeat(64))
                ));
            }

            @ParameterizedTest(name = "{0} - \"{1}\"")
            @MethodSource("validNicknames")
            @DisplayName("기존 1~64자 닉네임은 검증을 통과한다")
            void validate_existingNicknameWithin64Characters_hasNoViolations(Class<?> dto, String nickname) {
                // given
                String propertyName = "nickname";

                // when
                var violations = validator.validateValue(dto, propertyName, nickname);

                // then
                assertThat(violations).isEmpty();
            }
        }
    }
}
