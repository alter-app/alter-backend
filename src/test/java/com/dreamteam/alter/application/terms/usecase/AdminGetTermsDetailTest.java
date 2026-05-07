package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
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
class AdminGetTermsDetailTest {

    @Mock
    private TermsQueryRepository termsQueryRepository;

    @InjectMocks
    private AdminGetTermsDetail adminGetTermsDetail;

    @Test
    @DisplayName("존재하는 id 조회 성공")
    void getTermsDetail_success() {
        // given
        Terms terms = Terms.create(TermsType.SERVICE, "1.0", "서비스 이용약관", "https://notion.so/terms", true);
        when(termsQueryRepository.findById(1L)).thenReturn(Optional.of(terms));

        // when
        Terms result = adminGetTermsDetail.execute(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TermsType.SERVICE);
        assertThat(result.getVersion()).isEqualTo("1.0");
        assertThat(result.getTitle()).isEqualTo("서비스 이용약관");
        assertThat(result.getDocUrl()).isEqualTo("https://notion.so/terms");
        assertThat(result.getStatus()).isEqualTo(TermsStatus.DRAFT);
        assertThat(result.isRequired()).isTrue();
        verify(termsQueryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 id TERMS NOT FOUND")
    void getTermsDetail_throwsNotFound_whenTermsNotExists() {
        // given
        when(termsQueryRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminGetTermsDetail.execute(999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETED 상태 약관 조회시 TERMS NOT FOUND")
    void getTermsDetail_throwsNotFound_whenStatusIsDeleted() {
        // given
        // QueryDSL notDeleted() 조건으로 인해 DELETED 레코드를 반환하지 않음
        when(termsQueryRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminGetTermsDetail.execute(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
