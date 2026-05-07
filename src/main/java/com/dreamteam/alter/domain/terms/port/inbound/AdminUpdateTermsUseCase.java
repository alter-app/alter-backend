package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.domain.terms.command.AdminUpdateTermsCommand;

public interface AdminUpdateTermsUseCase {

    void execute(Long id, AdminUpdateTermsCommand command);
}
