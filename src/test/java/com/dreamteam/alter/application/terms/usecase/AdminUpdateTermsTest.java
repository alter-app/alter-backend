package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminUpdateTermsRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
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
class AdminUpdateTermsTest {

    @Mock
    private TermsRepository termsRepository;

    @InjectMocks
    private AdminUpdateTerms adminUpdateTerms;

    @Test
    void DRAFT_약관_수정_성공() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        AdminUpdateTermsRequestDto request = new AdminUpdateTermsRequestDto(
                "수정된 제목", "https://notion.so/terms-v2", false
        );
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when
        adminUpdateTerms.execute(1L, request, actor);

        // then
        assertThat(terms.getTitle()).isEqualTo("수정된 제목");
        assertThat(terms.getNotionUrl()).isEqualTo("https://notion.so/terms-v2");
        assertThat(terms.isRequired()).isFalse();
        verify(termsRepository, times(1)).findById(1L);
    }

    @Test
    void 존재하지_않는_id_TERMS_NOT_FOUND() {
        // given
        AdminUpdateTermsRequestDto request = new AdminUpdateTermsRequestDto(
                "수정된 제목", "https://notion.so/terms-v2", false
        );
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUpdateTerms.execute(999L, request, actor))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TERMS_NOT_FOUND);
    }

    @Test
    void PUBLISHED_상태_약관_수정시_TERMS_NOT_EDITABLE() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.publish();
        AdminUpdateTermsRequestDto request = new AdminUpdateTermsRequestDto(
                "수정된 제목", "https://notion.so/terms-v2", false
        );
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminUpdateTerms.execute(1L, request, actor))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TERMS_NOT_EDITABLE);
    }

    @Test
    void DEPRECATED_상태_약관_수정시_TERMS_NOT_EDITABLE() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.publish();
        terms.deprecate();
        AdminUpdateTermsRequestDto request = new AdminUpdateTermsRequestDto(
                "수정된 제목", "https://notion.so/terms-v2", false
        );
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminUpdateTerms.execute(1L, request, actor))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TERMS_NOT_EDITABLE);
    }
}
