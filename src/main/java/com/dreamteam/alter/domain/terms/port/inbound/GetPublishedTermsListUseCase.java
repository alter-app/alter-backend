package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.domain.terms.result.GetPublishedTermsListResult;

import java.util.List;

public interface GetPublishedTermsListUseCase {

    List<GetPublishedTermsListResult> execute();
}
