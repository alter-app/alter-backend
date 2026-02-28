package com.dreamteam.alter.application.email.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailSendEvent {
    private Long logId;
    private String email;
    private String code;
}
