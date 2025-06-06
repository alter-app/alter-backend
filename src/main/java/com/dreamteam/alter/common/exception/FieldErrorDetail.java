package com.dreamteam.alter.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorDetail {

    private String field;

    private String message;

}
