package com.dreamteam.alter.adapter.inbound.general.auth.dto;

import com.dreamteam.alter.domain.user.type.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {

    private SocialProvider provider;

    private String socialId;

    private String email;

}
