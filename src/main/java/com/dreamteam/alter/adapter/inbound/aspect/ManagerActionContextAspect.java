package com.dreamteam.alter.adapter.inbound.aspect;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
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
public class ManagerActionContextAspect {

    private final ManagerUserQueryRepository managerUserQueryRepository;

    @Around("execution(public * com.dreamteam.alter.adapter.inbound.manager.*.controller.*Controller.*(..))")
    public Object resolveManagerContext(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isNotEmpty(authentication) && authentication.getDetails() instanceof LoginUserDto loginUser) {
            // userId 기반으로 ManagerUser 조회
            ManagerUser managerUser = managerUserQueryRepository.findByUserId(loginUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            ManagerActor managerActor = ManagerActor.from(
                managerUser,
                authentication.getAuthorities().stream().toList()
            );

            ManagerActionContext context = ManagerActionContext.getInstance();
            context.setActor(managerActor);
        }

        Object result = joinPoint.proceed();

        ManagerActionContext.clear();

        return result;
    }
}
