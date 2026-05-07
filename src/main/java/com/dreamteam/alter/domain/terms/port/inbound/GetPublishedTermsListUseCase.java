package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.domain.terms.entity.Terms;

import java.util.List;

public interface GetPublishedTermsListUseCase {

    List<Terms> execute();
}
