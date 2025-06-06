package com.dreamteam.alter.adapter.inbound.aspect;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.application.aop.AdminActionContext;
import com.dreamteam.alter.application.auth.token.AccessTokenAuthentication;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminActionContextAspect {

    private final UserQueryRepository userQueryRepository;

    @Around("execution(public * com.dreamteam.alter.adapter.inbound.admin.*.controller.*Controller.*(..))")
    public Object resolveAdminContext(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        if (ObjectUtils.isNotEmpty(authentication) && AccessTokenAuthentication.class.isAssignableFrom(authentication.getClass())) {
            LoginUserDto loginUser = (LoginUserDto) authentication.getDetails();

            User actorUser = userQueryRepository.findById(loginUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            AdminActor adminActor = AdminActor.from(
                actorUser,
                authentication.getAuthorities()
                    .stream()
                    .toList()
            );

            AdminActionContext context = AdminActionContext.getInstance();
            context.setActor(adminActor);
        }

        Object result = joinPoint.proceed();

        AdminActionContext.clear();

        return result;
    }

}
