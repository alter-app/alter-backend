package com.dreamteam.alter.application.terms.service;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.type.TermsType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("TermsAgreementValidator 테스트")
class TermsAgreementValidatorTests {

    @Mock
    private TermsQueryRepository termsQueryRepository;

    @InjectMocks
    private TermsAgreementValidator termsAgreementValidator;

    private Terms createTermsMock(TermsType type, boolean required) {
        Terms terms = mock(Terms.class);
        given(terms.getType()).willReturn(type);
        given(terms.isRequired()).willReturn(required);
        return terms;
    }

    @Nested
    @DisplayName("validateAndResolve")
    class ValidateAndResolveTests {

        @Test
        @DisplayName("필수 약관을 모두 동의한 경우 Terms 목록 반환")
        void validateAndResolve_allRequiredAgreed_returnsTermsList() {
            // given
            Terms serviceTerms = createTermsMock(TermsType.SERVICE, true);
            Terms privacyTerms = createTermsMock(TermsType.PRIVACY, true);
            given(termsQueryRepository.findLatestPublishedPerType()).willReturn(List.of(serviceTerms, privacyTerms));

            // when
            List<Terms> result = termsAgreementValidator.validateAndResolve(
                Set.of(TermsType.SERVICE, TermsType.PRIVACY)
            );

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("필수 약관 외 선택 약관도 함께 동의한 경우 전체 Terms 목록 반환")
        void validateAndResolve_requiredAndOptionalAgreed_returnsAllTerms() {
            // given
            Terms serviceTerms = createTermsMock(TermsType.SERVICE, true);
            Terms marketingTerms = createTermsMock(TermsType.MARKETING, false);
            given(termsQueryRepository.findLatestPublishedPerType()).willReturn(List.of(serviceTerms, marketingTerms));

            // when
            List<Terms> result = termsAgreementValidator.validateAndResolve(
                Set.of(TermsType.SERVICE, TermsType.MARKETING)
            );

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("필수 약관 미동의 시 REQUIRED_TERMS_NOT_AGREED 예외 발생")
        void validateAndResolve_missingRequiredTerms_throwsRequiredTermsNotAgreed() {
            // given
            Terms serviceTerms = createTermsMock(TermsType.SERVICE, true);
            Terms privacyTerms = createTermsMock(TermsType.PRIVACY, true);
            given(termsQueryRepository.findLatestPublishedPerType()).willReturn(List.of(serviceTerms, privacyTerms));

            // when & then
            assertThatThrownBy(() -> termsAgreementValidator.validateAndResolve(Set.of(TermsType.SERVICE)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.REQUIRED_TERMS_NOT_AGREED));
        }

        @Test
        @DisplayName("PUBLISHED 되지 않은 타입 동의 시 해당 타입 무시하고 동의된 타입만 반환")
        void validateAndResolve_unpublishedTypeAgreed_ignoresUnpublishedType() {
            // given - LOCATION은 PUBLISHED 약관 없음 (findLatestPublishedPerType 결과에 미포함)
            Terms serviceTerms = createTermsMock(TermsType.SERVICE, true);
            given(termsQueryRepository.findLatestPublishedPerType()).willReturn(List.of(serviceTerms));

            // when - LOCATION 타입도 함께 동의했지만 PUBLISHED 약관이 없으므로 무시됨
            List<Terms> result = termsAgreementValidator.validateAndResolve(
                Set.of(TermsType.SERVICE, TermsType.LOCATION)
            );

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("필수 약관이 없는 경우 동의 목록 없어도 통과")
        void validateAndResolve_noRequiredTerms_emptyAgreedSetPasses() {
            // given
            given(termsQueryRepository.findLatestPublishedPerType()).willReturn(List.of());

            // when
            List<Terms> result = termsAgreementValidator.validateAndResolve(Set.of());

            // then
            assertThat(result).isEmpty();
        }
    }
}
