package com.dreamteam.alter.domain.terms.port.outbound;

import com.dreamteam.alter.domain.terms.entity.UserTermsAgreement;

import java.util.List;

public interface UserTermsAgreementRepository {
    List<UserTermsAgreement> saveAll(List<UserTermsAgreement> agreements);
}
