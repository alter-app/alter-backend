package com.dreamteam.alter.domain.posting.command;

import com.dreamteam.alter.domain.posting.type.PaymentType;

import java.util.List;

public record CreatePostingCommand(
    Long workspaceId,
    String title,
    String description,
    int payAmount,
    PaymentType paymentType,
    List<PostingScheduleCommand> schedules
) {}
