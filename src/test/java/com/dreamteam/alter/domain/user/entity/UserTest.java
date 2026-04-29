package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.domain.user.type.UserGender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("create() 호출 후 addUserSocial() 호출 시 NPE가 발생하지 않는다")
    void create_addUserSocial_NPE_발생하지_않음() {
        // given
        User user = User.create("01012345678", "encoded-password", "김철수", "닉네임", UserGender.GENDER_MALE, "19900101", "test@example.com");
        UserSocial userSocial = mock(UserSocial.class);

        // when & then
        assertThatCode(() -> user.addUserSocial(userSocial)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("create() 호출 후 addCertificate() 호출 시 NPE가 발생하지 않는다")
    void create_addCertificate_NPE_발생하지_않음() {
        // given
        User user = User.create("01012345678", "encoded-password", "김철수", "닉네임", UserGender.GENDER_MALE, "19900101", "test@example.com");
        UserCertificate certificate = mock(UserCertificate.class);

        // when & then
        assertThatCode(() -> user.addCertificate(certificate)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("createWithSocial() 호출 후 addUserSocial() 호출 시 NPE가 발생하지 않는다")
    void createWithSocial_addUserSocial_NPE_발생하지_않음() {
        // given
        User user = User.createWithSocial("01012345678", "김철수", "닉네임", UserGender.GENDER_MALE, "19900101", "test@example.com");
        UserSocial userSocial = mock(UserSocial.class);

        // when & then
        assertThatCode(() -> user.addUserSocial(userSocial)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("createWithSocial() 호출 후 addCertificate() 호출 시 NPE가 발생하지 않는다")
    void createWithSocial_addCertificate_NPE_발생하지_않음() {
        // given
        User user = User.createWithSocial("01012345678", "김철수", "닉네임", UserGender.GENDER_MALE, "19900101", "test@example.com");
        UserCertificate certificate = mock(UserCertificate.class);

        // when & then
        assertThatCode(() -> user.addCertificate(certificate)).doesNotThrowAnyException();
    }
}
