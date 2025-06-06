package com.dreamteam.alter.application.aop;

import com.dreamteam.alter.domain.user.context.AdminActor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.ObjectUtils;

@Data
@Aspect
@RequiredArgsConstructor
public class AdminActionContext {

    private static final ThreadLocal<AdminActionContext> context = ThreadLocal.withInitial(AdminActionContext::new);

    private AdminActor actor = null;

    private Boolean isAnonymous() {
        return ObjectUtils.isEmpty(actor);
    }

    public static AdminActionContext getInstance()  {
        if (context.get() == null) {
            context.set(new AdminActionContext());
        }

        return context.get();
    }

    public static void clear() {
        context.remove();
    }

}
