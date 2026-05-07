package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.domain.terms.command.AdminUpdateTermsCommand;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import com.dreamteam.alter.domain.terms.type.TermsType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUpdateTermsTest {

    @Mock
    private TermsRepository termsRepository;

    @InjectMocks
    private AdminUpdateTerms adminUpdateTerms;

    @Test
    @DisplayName("DRAFT 약관 수정 성공")
    void updateTerms_success_whenStatusIsDraft() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "1.0", "서비스 이용약관", "https://notion.so/terms", true);
        AdminUpdateTermsCommand command = new AdminUpdateTermsCommand(
                "수정된 제목", "https://notion.so/terms-v2", false
        );
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when
        adminUpdateTerms.execute(1L, command);

        // then
        assertThat(terms.getTitle()).isEqualTo("수정된 제목");
        assertThat(terms.getDocUrl()).isEqualTo("https://notion.so/terms-v2");
        assertThat(terms.isRequired()).isFalse();
        verify(termsRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 id TERMS NOT FOUND")
    void updateTerms_throwsNotFound_whenTermsNotExists() {
        // given
        AdminUpdateTermsCommand command = new AdminUpdateTermsCommand(
                "수정된 제목", "https://notion.so/terms-v2", false
        );
        when(termsRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUpdateTerms.execute(999L, command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("PUBLISHED 상태 약관 수정시 TERMS NOT EDITABLE")
    void updateTerms_throwsConflict_whenStatusIsPublished() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.publish();
        AdminUpdateTermsCommand command = new AdminUpdateTermsCommand(
                "수정된 제목", "https://notion.so/terms-v2", false
        );
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminUpdateTerms.execute(1L, command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    @DisplayName("DEPRECATED 상태 약관 수정시 TERMS NOT EDITABLE")
    void updateTerms_throwsConflict_whenStatusIsDeprecated() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.publish();
        terms.deprecate();
        AdminUpdateTermsCommand command = new AdminUpdateTermsCommand(
                "수정된 제목", "https://notion.so/terms-v2", false
        );
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminUpdateTerms.execute(1L, command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
