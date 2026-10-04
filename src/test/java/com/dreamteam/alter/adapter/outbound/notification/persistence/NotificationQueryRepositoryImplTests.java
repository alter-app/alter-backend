package com.dreamteam.alter.adapter.outbound.notification.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.Notification;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.UserGender;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({QueryDslConfig.class, NotificationQueryRepositoryImpl.class})
class NotificationQueryRepositoryImplTests {
    @Autowired EntityManager em;
    @Autowired NotificationQueryRepositoryImpl notifications;

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
        "APP,NULL,NULL,4", "APP,NULL,false,2", "APP,NULL,true,2",
        "APP,GENERAL,NULL,2", "APP,GENERAL,false,1", "APP,GENERAL,true,1",
        "APP,SCHEDULE,NULL,2", "APP,SCHEDULE,false,1", "APP,SCHEDULE,true,1",
        "MANAGER,NULL,NULL,4", "MANAGER,NULL,false,2", "MANAGER,NULL,true,2",
        "MANAGER,GENERAL,NULL,2", "MANAGER,GENERAL,false,1", "MANAGER,GENERAL,true,1",
        "MANAGER,SCHEDULE,NULL,2", "MANAGER,SCHEDULE,false,1", "MANAGER,SCHEDULE,true,1"
    })
    void listCountAndUnreadBadgePreserveUserScopeAndFilterMeaning(
        TokenScope scope, NotificationType type, Boolean isRead, int expected
    ) {
        User user = user();
        User other = user();
        for (TokenScope fixtureScope : List.of(TokenScope.APP, TokenScope.MANAGER)) {
            for (NotificationType fixtureType : List.of(NotificationType.GENERAL, NotificationType.SCHEDULE)) {
                Notification unread = Notification.create(user, fixtureScope, fixtureType, null, "미읽음", "본문");
                Notification read = Notification.create(user, fixtureScope, fixtureType, null, "읽음", "본문");
                read.markAsRead();
                em.persist(unread);
                em.persist(read);
                em.persist(Notification.create(other, fixtureScope, fixtureType, null, "다른 사용자", "본문"));
            }
        }

        var result = notifications.getNotificationsWithCursor(CursorPageRequest.of(null, 20), user, scope, type, isRead);
        assertThat(result).hasSize(expected);
        assertThat(notifications.getCountOfNotifications(user, scope, type, isRead)).isEqualTo(expected);
        assertThat(result).allSatisfy(notification -> {
            if (type != null) assertThat(notification.type()).isEqualTo(type);
            if (isRead != null) assertThat(notification.isRead()).isEqualTo(isRead);
            assertThat(notification.title()).isNotEqualTo("다른 사용자");
        });
        assertThat(notifications.getCountOfUnreadNotifications(user, scope)).isEqualTo(2)
            .isEqualTo(notifications.getCountOfNotifications(user, scope, null, false));
        assertThat(notifications.findUnreadNotifications(user, scope)).hasSize(2);
    }

    private User user() {
        String unique = String.valueOf(System.nanoTime());
        User user = User.create("010" + unique.substring(0, 8), "encoded", "알림", "query" + unique,
            UserGender.GENDER_MALE, "19990101", null);
        em.persist(user);
        return user;
    }
}
