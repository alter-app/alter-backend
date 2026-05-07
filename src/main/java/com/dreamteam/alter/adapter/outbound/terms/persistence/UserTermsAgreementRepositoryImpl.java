package com.dreamteam.alter.adapter.outbound.terms.persistence;

import com.dreamteam.alter.domain.terms.entity.UserTermsAgreement;
import com.dreamteam.alter.domain.terms.port.outbound.UserTermsAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserTermsAgreementRepositoryImpl implements UserTermsAgreementRepository {

    private final UserTermsAgreementJpaRepository userTermsAgreementJpaRepository;

    @Override
    public List<UserTermsAgreement> saveAll(List<UserTermsAgreement> agreements) {
        return userTermsAgreementJpaRepository.saveAll(agreements);
    }
}
