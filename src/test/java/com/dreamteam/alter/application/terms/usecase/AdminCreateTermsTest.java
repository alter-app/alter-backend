package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.AdminCreateTermsRequestDto;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsRepository;
import com.dreamteam.alter.domain.terms.type.TermsType;
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
    void 유효한_요청으로_약관_생성_후_id_반환() {
        // given
        AdminCreateTermsRequestDto request = new AdminCreateTermsRequestDto(
                TermsType.SERVICE, "v1.0", "서비스 이용약관", "https://notion.so/terms", true
        );
        Terms savedTerms = mock(Terms.class);
        when(savedTerms.getId()).thenReturn(1L);
        when(termsRepository.save(any(Terms.class))).thenReturn(savedTerms);

        // when
        Long id = adminCreateTerms.execute(request);

        // then
        assertThat(id).isEqualTo(1L);
        verify(termsRepository, times(1)).save(any(Terms.class));
    }

    @Test
    void 유효한_TermsType_enum으로_약관_생성_성공() {
        // given
        AdminCreateTermsRequestDto request = new AdminCreateTermsRequestDto(
                TermsType.PRIVACY, "v1.0", "개인정보 처리방침", "https://notion.so/privacy", true
        );
        Terms savedTerms = mock(Terms.class);
        when(savedTerms.getId()).thenReturn(2L);
        when(termsRepository.save(any(Terms.class))).thenReturn(savedTerms);

        // when
        Long id = adminCreateTerms.execute(request);

        // then
        ArgumentCaptor<Terms> termsCaptor = ArgumentCaptor.forClass(Terms.class);
        verify(termsRepository).save(termsCaptor.capture());
        assertThat(termsCaptor.getValue().getType()).isEqualTo(TermsType.PRIVACY);
        assertThat(id).isEqualTo(2L);
    }
}
