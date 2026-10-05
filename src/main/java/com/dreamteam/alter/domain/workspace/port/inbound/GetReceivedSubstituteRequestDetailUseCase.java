package com.dreamteam.alter.domain.workspace.port.inbound;

import com.dreamteam.alter.domain.workspace.command.GetReceivedSubstituteRequestDetailCommand;
import com.dreamteam.alter.domain.workspace.result.ReceivedSubstituteRequestDetailResult;

public interface GetReceivedSubstituteRequestDetailUseCase {
    ReceivedSubstituteRequestDetailResult execute(GetReceivedSubstituteRequestDetailCommand command);
}
