package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.GetLinkedSocialAccountsUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.result.SocialAccountStatusResult;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service("getLinkedSocialAccounts")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLinkedSocialAccounts implements GetLinkedSocialAccountsUseCase {

    private final UserSocialQueryRepository userSocialQueryRepository;

    @Override
    public List<SocialAccountStatusResult> execute(User user) {
        Map<SocialProvider, LocalDateTime> linked = userSocialQueryRepository
            .findLinkedSocialAccountsByUser(user);

        return Arrays.stream(SocialProvider.values())
            .map(provider -> new SocialAccountStatusResult(
                provider,
                linked.containsKey(provider),
                linked.get(provider)
            ))
            .toList();
    }
}
