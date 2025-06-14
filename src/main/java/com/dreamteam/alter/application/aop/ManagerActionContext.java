package com.dreamteam.alter.application.aop;

import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.ObjectUtils;

@Data
@Aspect
@RequiredArgsConstructor
public class ManagerActionContext {
    private static final ThreadLocal<ManagerActionContext> context = ThreadLocal.withInitial(ManagerActionContext::new);

    private ManagerActor actor = null;

    private Boolean isAnonymous() {
        return ObjectUtils.isEmpty(actor);
    }

    public static ManagerActionContext getInstance()  {
        if (context.get() == null) {
            context.set(new ManagerActionContext());
        }
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
