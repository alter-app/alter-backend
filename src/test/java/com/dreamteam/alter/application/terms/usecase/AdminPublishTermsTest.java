package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
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
class AdminPublishTermsTest {

    @Mock
    private TermsRepository termsRepository;

    @InjectMocks
    private AdminPublishTerms adminPublishTerms;

    @Test
    @DisplayName("DRAFT 약관 게시 성공 기존 PUBLISHED 없음")
    void publishTerms_success_whenNoPreviousPublished() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));
        when(termsRepository.findPublishedByTypeWithLock(TermsType.SERVICE)).thenReturn(Optional.empty());

        // when
        adminPublishTerms.execute(1L);

        // then
        assertThat(terms.getStatus()).isEqualTo(TermsStatus.PUBLISHED);
        assertThat(terms.getEffectiveAt()).isNotNull();
        verify(termsRepository, times(1)).findById(1L);
        verify(termsRepository, times(1)).findPublishedByTypeWithLock(TermsType.SERVICE);
    }

    @Test
    @DisplayName("DRAFT 약관 게시 성공 기존 PUBLISHED 있음")
    void publishTerms_success_deprecatesPreviousPublished() {
        // given
        Terms existingTerms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        existingTerms.publish();
        Terms newTerms = Terms.create(TermsType.SERVICE, "v2.0", "서비스 이용약관 v2", "https://notion.so/terms-v2", true);
        when(termsRepository.findById(2L)).thenReturn(Optional.of(newTerms));
        when(termsRepository.findPublishedByTypeWithLock(TermsType.SERVICE)).thenReturn(Optional.of(existingTerms));

        // when
        adminPublishTerms.execute(2L);

        // then
        assertThat(existingTerms.getStatus()).isEqualTo(TermsStatus.DEPRECATED);
        assertThat(newTerms.getStatus()).isEqualTo(TermsStatus.PUBLISHED);
        assertThat(newTerms.getEffectiveAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 id TERMS NOT FOUND")
    void publishTerms_throwsNotFound_whenTermsNotExists() {
        // given
        when(termsRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminPublishTerms.execute(999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("PUBLISHED 상태 약관 게시 시도시 TERMS NOT PUBLISHABLE")
    void publishTerms_throwsConflict_whenStatusIsPublished() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.publish();
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminPublishTerms.execute(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    @DisplayName("DEPRECATED 상태 약관 게시 시도시 TERMS NOT PUBLISHABLE")
    void publishTerms_throwsConflict_whenStatusIsDeprecated() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.publish();
        terms.deprecate();
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminPublishTerms.execute(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
