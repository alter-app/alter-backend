package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.domain.terms.entity.Terms;

public interface AdminGetTermsDetailUseCase {

    Terms execute(Long id);
}
