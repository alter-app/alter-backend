package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CheckEmailDuplicationRequestDto;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckEmailDuplicationTests {
    @Mock UserQueryRepository users;
    @InjectMocks CheckEmailDuplication checkEmailDuplication;

    @Test
    void mixedCaseEmailIsLookedUpNormalizedAndEchoedAsRequested() {
        when(users.findByEmail("foo@example.com")).thenReturn(Optional.of(mock(User.class)));
        var response = checkEmailDuplication.execute(new CheckEmailDuplicationRequestDto(" Foo@Example.com "));
        verify(users).findByEmail("foo@example.com");
        assertThat(response.getEmail()).isEqualTo(" Foo@Example.com ");
        assertThat(response.isDuplicated()).isTrue();
    }

    @Test
    void unusedEmailIsNotDuplicated() {
        when(users.findByEmail("free@example.com")).thenReturn(Optional.empty());
        var response = checkEmailDuplication.execute(new CheckEmailDuplicationRequestDto("Free@example.com"));
        assertThat(response.isDuplicated()).isFalse();
    }
}
