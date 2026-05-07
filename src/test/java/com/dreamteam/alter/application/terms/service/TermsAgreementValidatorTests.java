package com.dreamteam.alter.application.terms.service;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("TermsAgreementValidator 테스트")
class TermsAgreementValidatorTests {

    @Mock
    private TermsQueryRepository termsQueryRepository;

    @InjectMocks
    private TermsAgreementValidator termsAgreementValidator;

    private Terms createTermsMock(Long id) {
        Terms terms = mock(Terms.class);
        given(terms.getId()).willReturn(id);
        return terms;
    }

    @Nested
    @DisplayName("validateAndResolve")
    class ValidateAndResolveTests {

        @Test
        @DisplayName("필수 약관을 모두 동의한 경우 Terms 목록 반환")
        void validateAndResolve_allRequiredAgreed_returnsTermsList() {
            // given
            Terms terms1 = createTermsMock(1L);
            Terms terms2 = createTermsMock(2L);
            given(termsQueryRepository.findAllRequiredPublished()).willReturn(List.of(terms1, terms2));
            given(termsQueryRepository.findById(1L)).willReturn(Optional.of(terms1));
            given(termsQueryRepository.findById(2L)).willReturn(Optional.of(terms2));

            // when
            List<Terms> result = termsAgreementValidator.validateAndResolve(List.of(1L, 2L));

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("필수 약관 외 선택 약관도 함께 동의한 경우 전체 Terms 목록 반환")
        void validateAndResolve_requiredAndOptionalAgreed_returnsAllTerms() {
            // given
            Terms requiredTerms = createTermsMock(1L);
            Terms optionalTerms = mock(Terms.class); // 필수 목록에 없으므로 getId() 호출 안 됨
            given(termsQueryRepository.findAllRequiredPublished()).willReturn(List.of(requiredTerms));
            given(termsQueryRepository.findById(1L)).willReturn(Optional.of(requiredTerms));
            given(termsQueryRepository.findById(2L)).willReturn(Optional.of(optionalTerms));

            // when
            List<Terms> result = termsAgreementValidator.validateAndResolve(List.of(1L, 2L));

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("필수 약관 미동의 시 REQUIRED_TERMS_NOT_AGREED 예외 발생")
        void validateAndResolve_missingRequiredTerms_throwsRequiredTermsNotAgreed() {
            // given
            Terms terms1 = createTermsMock(1L);
            Terms terms2 = createTermsMock(2L);
            given(termsQueryRepository.findAllRequiredPublished()).willReturn(List.of(terms1, terms2));

            // when & then
            assertThatThrownBy(() -> termsAgreementValidator.validateAndResolve(List.of(1L)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.REQUIRED_TERMS_NOT_AGREED));

            then(termsQueryRepository).should(never()).findById(any());
        }

        @Test
        @DisplayName("동의한 약관 ID가 존재하지 않는 경우 TERMS_NOT_FOUND 예외 발생")
        void validateAndResolve_nonExistentTermsId_throwsTermsNotFound() {
            // given
            Terms terms1 = createTermsMock(1L);
            given(termsQueryRepository.findAllRequiredPublished()).willReturn(List.of(terms1));
            given(termsQueryRepository.findById(1L)).willReturn(Optional.of(terms1));
            given(termsQueryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> termsAgreementValidator.validateAndResolve(List.of(1L, 999L)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.TERMS_NOT_FOUND));
        }

        @Test
        @DisplayName("필수 약관이 없는 경우 동의 목록 없어도 통과")
        void validateAndResolve_noRequiredTerms_emptyAgreedListPasses() {
            // given
            given(termsQueryRepository.findAllRequiredPublished()).willReturn(List.of());

            // when
            List<Terms> result = termsAgreementValidator.validateAndResolve(List.of());

            // then
            assertThat(result).isEmpty();
        }
    }
}
