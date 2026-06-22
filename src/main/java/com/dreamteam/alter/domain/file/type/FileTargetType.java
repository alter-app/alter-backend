package com.dreamteam.alter.domain.file.type;

public enum FileTargetType {
    USER_PROFILE,
    USER_CERTIFICATE,
    POSTING,
    WORKSPACE,
    WORKSPACE_REPRESENTATIVE_IMAGE, // 업장 대표이미지 (최대 5개)
    WORKSPACE_CERTIFICATE,      // 사업자등록증명원
    WORKSPACE_OWN_IDENTITY,     // 대표자 신분증 사본
    WORKSPACE_WARRANT,          // 위임 확인서
    WORKSPACE_REQUEST_COMMENT,
    CHAT_MESSAGE
}
