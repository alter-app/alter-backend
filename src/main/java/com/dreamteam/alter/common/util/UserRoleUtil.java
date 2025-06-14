package com.dreamteam.alter.common.util;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.user.type.UserRole;

public class UserRoleUtil {

    public static UserRole getRoleForScope(Authorization authorization) {
        UserRole requiredRole = switch (authorization.getScope()) {
            case MANAGER -> UserRole.ROLE_MANAGER;
            case ADMIN -> UserRole.ROLE_ADMIN;
            default -> UserRole.ROLE_USER;
        };
        if (!authorization.getUser().getRoles().contains(requiredRole)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return requiredRole;
    }

}
