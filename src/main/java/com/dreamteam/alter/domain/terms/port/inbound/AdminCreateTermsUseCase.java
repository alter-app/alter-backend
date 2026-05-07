package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.domain.terms.command.AdminCreateTermsCommand;

public interface AdminCreateTermsUseCase {

    Long execute(AdminCreateTermsCommand command);
}
