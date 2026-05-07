package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.domain.terms.command.AdminCreateTermsCommand;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import com.dreamteam.alter.domain.terms.type.TermsType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCreateTermsTest {

    @Mock
    private TermsRepository termsRepository;

    @InjectMocks
    private AdminCreateTerms adminCreateTerms;

    @Test
    @DisplayName("유효한 요청으로 약관 생성 후 id 반환")
    void createTerms_returnsId() {
        // given
        AdminCreateTermsCommand command = new AdminCreateTermsCommand(
                TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true
        );
        Terms savedTerms = mock(Terms.class);
        when(savedTerms.getId()).thenReturn(1L);
        when(termsRepository.save(any(Terms.class))).thenReturn(savedTerms);

        // when
        Long id = adminCreateTerms.execute(command);

        // then
        assertThat(id).isEqualTo(1L);
        verify(termsRepository, times(1)).save(any(Terms.class));
    }

    @Test
    @DisplayName("유효한 TermsType enum으로 약관 생성 성공")
    void createTerms_success_withValidTermsTypeEnum() {
        // given
        AdminCreateTermsCommand command = new AdminCreateTermsCommand(
                TermsType.PRIVACY, "v1.0", "개인정보 처리방침", "https://notion.so/privacy", true
        );
        Terms savedTerms = mock(Terms.class);
        when(savedTerms.getId()).thenReturn(2L);
        when(termsRepository.save(any(Terms.class))).thenReturn(savedTerms);

        // when
        Long id = adminCreateTerms.execute(command);

        // then
        ArgumentCaptor<Terms> termsCaptor = ArgumentCaptor.forClass(Terms.class);
        verify(termsRepository).save(termsCaptor.capture());
        assertThat(termsCaptor.getValue().getType()).isEqualTo(TermsType.PRIVACY);
        assertThat(id).isEqualTo(2L);
    }
}
