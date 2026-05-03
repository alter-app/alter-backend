package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import com.dreamteam.alter.domain.user.context.AdminActor;
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

    @Mock
    private TermsQueryRepository termsQueryRepository;

    @InjectMocks
    private AdminPublishTerms adminPublishTerms;

    @Test
    void DRAFT_약관_게시_성공_기존_PUBLISHED_없음() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));
        when(termsQueryRepository.findPublishedByType("SERVICE")).thenReturn(Optional.empty());

        // when
        adminPublishTerms.execute(1L, actor);

        // then
        assertThat(terms.getStatus()).isEqualTo(TermsStatus.PUBLISHED);
        assertThat(terms.getEffectiveAt()).isNotNull();
        verify(termsRepository, times(1)).findById(1L);
        verify(termsQueryRepository, times(1)).findPublishedByType("SERVICE");
    }

    @Test
    void DRAFT_약관_게시_성공_기존_PUBLISHED_있음() {
        // given
        Terms existingTerms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        existingTerms.publish();
        Terms newTerms = Terms.create(TermsType.SERVICE, "v2.0", "서비스 이용약관 v2", "https://notion.so/terms-v2", true);
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(2L)).thenReturn(Optional.of(newTerms));
        when(termsQueryRepository.findPublishedByType("SERVICE")).thenReturn(Optional.of(existingTerms));

        // when
        adminPublishTerms.execute(2L, actor);

        // then
        assertThat(existingTerms.getStatus()).isEqualTo(TermsStatus.DEPRECATED);
        assertThat(newTerms.getStatus()).isEqualTo(TermsStatus.PUBLISHED);
        assertThat(newTerms.getEffectiveAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_id_TERMS_NOT_FOUND() {
        // given
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminPublishTerms.execute(999L, actor))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void PUBLISHED_상태_약관_게시_시도시_TERMS_NOT_PUBLISHABLE() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.publish();
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminPublishTerms.execute(1L, actor))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void DEPRECATED_상태_약관_게시_시도시_TERMS_NOT_PUBLISHABLE() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.publish();
        terms.deprecate();
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminPublishTerms.execute(1L, actor))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
