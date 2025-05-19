package com.dreamteam.alter.adapter.inbound.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CursorDto {

    private Long id;

    private LocalDateTime createdAt;

}
