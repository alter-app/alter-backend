package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.domain.user.command.RemoveEmailCommand;
import com.dreamteam.alter.domain.user.command.UpdateEmailCommand;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserGender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailAccountUseCaseTests {
    @Mock UserQueryRepository users;
    @Mock EmailVerificationSessionStoreRepository sessions;
    @InjectMocks UpdateEmail update;
    @InjectMocks RemoveEmail remove;

    @Test
    void expiredVerificationSessionCannotChangeEmail() {
        User user = user("before@example.com");
        when(sessions.getEmailBySession("expired")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> update.execute(new UpdateEmailCommand(user, "expired")))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        assertThat(user.getEmail()).isEqualTo("before@example.com");
        verifyNoInteractions(users);
    }

    @Test
    void verifiedEmailAlreadyUsedByAnotherUserCannotReplaceEmail() {
        User user = user("before@example.com");
        when(sessions.getEmailBySession("session")).thenReturn(Optional.of("used@example.com"));
        when(users.findByEmail("used@example.com")).thenReturn(Optional.of(user("used@example.com")));
        assertThatThrownBy(() -> update.execute(new UpdateEmailCommand(user, "session")))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.EMAIL_DUPLICATED);
        assertThat(user.getEmail()).isEqualTo("before@example.com");
        verify(sessions, never()).deleteSession(anyString());
    }

    @Test
    void matchingCurrentEmailKeepsExistingConflictContract() {
        User user = user("same@example.com");
        when(sessions.getEmailBySession("session")).thenReturn(Optional.of("same@example.com"));
        assertThatThrownBy(() -> update.execute(new UpdateEmailCommand(user, "session")))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.CONFLICT);
        verifyNoInteractions(users);
    }

    @Test
    void successfulRegistrationConsumesVerifiedSessionAndRemovalClearsEmail() {
        User user = user(null);
        when(sessions.getEmailBySession("session")).thenReturn(Optional.of("verified@example.com"));
        update.execute(new UpdateEmailCommand(user, "session"));
        assertThat(user.getEmail()).isEqualTo("verified@example.com");
        verify(sessions).deleteSession("session");
        remove.execute(new RemoveEmailCommand(user));
        assertThat(user.getEmail()).isNull();
    }

    @Test
    void successfulEmailChangeConsumesVerifiedSession() {
        User user = user("before@example.com");
        when(sessions.getEmailBySession("session")).thenReturn(Optional.of("after@example.com"));
        update.execute(new UpdateEmailCommand(user, "session"));
        assertThat(user.getEmail()).isEqualTo("after@example.com");
        verify(sessions).deleteSession("session");
    }

    @Test
    void removingMissingEmailKeepsExistingErrorContract() {
        assertThatThrownBy(() -> remove.execute(new RemoveEmailCommand(user(null))))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
    }

    private User user(String email) {
        return User.create("01000000000", "encoded", "검증", "email" + System.nanoTime(),
            UserGender.GENDER_MALE, "19990101", email);
    }
}
