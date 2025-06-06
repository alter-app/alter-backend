package com.dreamteam.alter.application.aop;

import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.ObjectUtils;

@Data
@Aspect
@RequiredArgsConstructor
public class AppActionContext {
    private static final ThreadLocal<AppActionContext> context = ThreadLocal.withInitial(AppActionContext::new);

    private AppActor actor = null;

    private Boolean isAnonymous() {
        return ObjectUtils.isEmpty(actor);
    }

    public static AppActionContext getInstance()  {
        if (context.get() == null) {
            context.set(new AppActionContext());
        }

        return context.get();
    }

    public static void clear() {
        context.remove();
    }

}
