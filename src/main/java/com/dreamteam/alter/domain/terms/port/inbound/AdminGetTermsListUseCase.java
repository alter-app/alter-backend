package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.query.AdminGetTermsListQuery;

import java.util.List;

public interface AdminGetTermsListUseCase {

    List<Terms> execute(AdminGetTermsListQuery query);

    long count(AdminGetTermsListQuery query);
}
