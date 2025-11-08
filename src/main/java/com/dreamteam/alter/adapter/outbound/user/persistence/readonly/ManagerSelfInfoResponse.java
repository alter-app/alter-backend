package com.dreamteam.alter.adapter.outbound.user.persistence.readonly;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerSelfInfoResponse {
    private Long id;
    private String name;
    private String nickname;
    private LocalDateTime createdAt;
}
