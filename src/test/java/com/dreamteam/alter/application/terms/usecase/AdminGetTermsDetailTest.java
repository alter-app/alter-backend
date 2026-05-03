package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminTermsDetailResponseDto;
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
class AdminGetTermsDetailTest {

    @Mock
    private TermsRepository termsRepository;

    @InjectMocks
    private AdminGetTermsDetail adminGetTermsDetail;

    @Test
    void 존재하는_id_조회_성공() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when
        AdminTermsDetailResponseDto result = adminGetTermsDetail.execute(1L, actor);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("SERVICE");
        assertThat(result.getVersion()).isEqualTo("v1.0");
        assertThat(result.getTitle()).isEqualTo("서비스 이용약관");
        assertThat(result.getNotionUrl()).isEqualTo("https://notion.so/terms");
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.isRequired()).isTrue();
        verify(termsRepository, times(1)).findById(1L);
    }

    @Test
    void 존재하지_않는_id_TERMS_NOT_FOUND() {
        // given
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminGetTermsDetail.execute(999L, actor))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TERMS_NOT_FOUND);
    }

    @Test
    void DELETED_상태_약관_조회시_TERMS_NOT_FOUND() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true);
        terms.delete();
        AdminActor actor = mock(AdminActor.class);
        when(termsRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when & then
        assertThatThrownBy(() -> adminGetTermsDetail.execute(1L, actor))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TERMS_NOT_FOUND);
    }
}
